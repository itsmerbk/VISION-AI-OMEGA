import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.awt.Desktop;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/**
 * ==============================================================================
 * VISION AI 2.0 — ALL-IN-ONE WHOLE COMPUTER & MOBILE COMMANDER
 * ==============================================================================
 * To run this single file directly:
 *     java VisionAgent.java
 *
 * Capabilities:
 *  - 3D Holographic Cyberpunk HUD (Three.js 8,000 particles & MediaPipe Hands)
 *  - Continuous Siri-Style Hands-Free Voice (Zero repeating, smart audio ducking)
 *  - Opens/Closes ANY Mac Application dynamically (Spotlight + Directory scanning)
 *  - Phone Calling & Voice Dialing (Android Vivo via ADB + Mac FaceTime / tel:)
 *  - WhatsApp & SMS Messaging with Auto-Send
 *  - Notification Monitoring & Verbal Auto-Reply Dialogue
 *  - Whole Computer Automation (Volume, Media, Screenshot, Screen Lock, Sleep)
 * ==============================================================================
 */
public class VisionAgent {
    private static final int PORT = 8080;
    private static volatile String lastSpoken = "Vision online.";
    private static String lastCommand = "";
    private static long lastCommandTime = 0;
    private static Process currentSayProcess = null;
    private static final Object speakLock = new Object();
    private static volatile boolean isSpeaking = false;

    // =========================================================================
    // 1. EMBEDDED CYBERPUNK 3D HOLOGRAPHIC HUD (HTML / CSS / JS)
    // =========================================================================
    private static final String HTML_CONTENT = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>VISION AI 2.0 - Computer & Mobile Commander</title>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <style>
            :root {
              --primary: #ff2a2a;
              --primary-glow: rgba(255, 42, 42, 0.45);
              --workplace: #00f0ff;
              --workplace-glow: rgba(0, 240, 255, 0.45);
              --bg-dark: #050608;
              --panel-bg: rgba(10, 15, 24, 0.75);
              --panel-border: rgba(255, 42, 42, 0.3);
              --text-dim: #8899aa;
              --text-bright: #ffffff;
              --accent-yellow: #ffd000;
              --accent-green: #00ff88;
            }

            * { box-sizing: border-box; }
            body {
              margin: 0;
              overflow: hidden;
              background-color: var(--bg-dark);
              color: var(--primary);
              font-family: 'SF Pro Display', -apple-system, 'Segoe UI', Roboto, 'Courier New', monospace;
              user-select: none;
            }

            body::before {
              content: " ";
              display: block;
              position: absolute;
              top: 0; left: 0; bottom: 0; right: 0;
              background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.25) 50%), linear-gradient(90deg, rgba(255, 0, 0, 0.03), rgba(0, 255, 0, 0.01), rgba(0, 0, 255, 0.03));
              z-index: 20;
              background-size: 100% 3px, 6px 100%;
              pointer-events: none;
            }

            #hud-top {
              position: absolute;
              top: 25px;
              left: 35px;
              right: 35px;
              z-index: 30;
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              pointer-events: none;
            }

            .title-block { pointer-events: auto; }
            h1 {
              margin: 0;
              font-size: 32px;
              letter-spacing: 6px;
              font-weight: 900;
              text-transform: uppercase;
              text-shadow: 0 0 20px var(--primary-glow);
              transition: color 0.5s, text-shadow 0.5s;
            }

            #status-badge {
              display: inline-flex;
              align-items: center;
              gap: 8px;
              font-size: 12px;
              color: var(--accent-green);
              letter-spacing: 2px;
              margin-top: 6px;
              text-transform: uppercase;
              background: rgba(0, 255, 136, 0.1);
              padding: 4px 10px;
              border: 1px solid rgba(0, 255, 136, 0.3);
              border-radius: 4px;
            }
            .pulse-dot {
              width: 8px;
              height: 8px;
              background: var(--accent-green);
              border-radius: 50%;
              animation: pulse 1.5s infinite;
            }
            @keyframes pulse {
              0% { opacity: 0.3; transform: scale(0.9); }
              50% { opacity: 1; transform: scale(1.2); }
              100% { opacity: 0.3; transform: scale(0.9); }
            }

            #voice-log {
              font-size: 15px;
              color: var(--accent-yellow);
              margin-top: 10px;
              font-weight: 600;
              letter-spacing: 1px;
              text-shadow: 0 0 10px rgba(255, 208, 0, 0.4);
              max-width: 550px;
            }
            #ai-response {
              font-size: 14px;
              color: #00f0ff;
              margin-top: 4px;
              letter-spacing: 1px;
              font-weight: 500;
              max-width: 550px;
            }

            .telemetry-panel {
              pointer-events: auto;
              background: var(--panel-bg);
              border: 1px solid var(--panel-border);
              backdrop-filter: blur(10px);
              padding: 14px 20px;
              border-radius: 8px;
              display: flex;
              flex-direction: column;
              gap: 8px;
              font-size: 12px;
              color: var(--text-dim);
              min-width: 270px;
              box-shadow: 0 8px 32px rgba(0,0,0,0.5);
            }
            .telemetry-row {
              display: flex;
              justify-content: space-between;
              align-items: center;
            }
            .telemetry-val {
              color: var(--text-bright);
              font-weight: 600;
            }

            #notification-banner {
              position: absolute;
              top: 110px;
              left: 50%;
              transform: translateX(-50%) translateY(-20px);
              z-index: 100;
              background: rgba(20, 5, 10, 0.94);
              border: 2px solid var(--primary);
              box-shadow: 0 0 35px var(--primary-glow);
              border-radius: 12px;
              padding: 20px 28px;
              width: 90%;
              max-width: 580px;
              backdrop-filter: blur(15px);
              display: none;
              opacity: 0;
              transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            }
            #notification-banner.active {
              display: block;
              opacity: 1;
              transform: translateX(-50%) translateY(0);
            }
            .notif-header {
              display: flex;
              align-items: center;
              justify-content: space-between;
              margin-bottom: 12px;
              border-bottom: 1px solid rgba(255, 42, 42, 0.2);
              padding-bottom: 8px;
            }
            .notif-app-tag {
              background: var(--primary);
              color: #000;
              font-size: 11px;
              font-weight: 800;
              padding: 3px 8px;
              border-radius: 4px;
              letter-spacing: 1.5px;
              text-transform: uppercase;
            }
            .notif-sender {
              font-size: 17px;
              font-weight: 700;
              color: #ffffff;
              letter-spacing: 1px;
            }
            .notif-body {
              font-size: 15px;
              color: #e0e0e0;
              margin-bottom: 16px;
              line-height: 1.4;
              background: rgba(255, 255, 255, 0.05);
              padding: 10px 14px;
              border-radius: 6px;
              border-left: 3px solid var(--primary);
            }
            .notif-prompt {
              font-size: 13px;
              color: var(--accent-yellow);
              margin-bottom: 12px;
              font-weight: 600;
              display: flex;
              align-items: center;
              gap: 8px;
            }
            .quick-replies {
              display: flex;
              flex-wrap: wrap;
              gap: 8px;
              margin-bottom: 14px;
            }
            .reply-chip {
              background: rgba(255, 255, 255, 0.1);
              border: 1px solid rgba(255, 255, 255, 0.2);
              color: #fff;
              padding: 6px 12px;
              border-radius: 20px;
              font-size: 12px;
              cursor: pointer;
              transition: all 0.2s;
            }
            .reply-chip:hover {
              background: var(--primary);
              color: #000;
              border-color: var(--primary);
              transform: translateY(-1px);
            }
            .notif-actions { display: flex; gap: 10px; }

            #controls {
              position: absolute;
              bottom: 30px;
              left: 35px;
              right: 35px;
              z-index: 30;
              display: flex;
              justify-content: space-between;
              align-items: center;
              pointer-events: none;
            }
            .btn-group {
              display: flex;
              gap: 12px;
              pointer-events: auto;
            }

            button {
              background: rgba(255, 42, 42, 0.15);
              color: var(--primary);
              border: 1px solid var(--primary);
              padding: 12px 20px;
              cursor: pointer;
              letter-spacing: 2px;
              font-size: 13px;
              font-weight: 700;
              border-radius: 6px;
              backdrop-filter: blur(8px);
              transition: all 0.25s ease;
              display: inline-flex;
              align-items: center;
              gap: 8px;
              text-transform: uppercase;
            }
            button:hover {
              background: var(--primary);
              color: #000;
              box-shadow: 0 0 18px var(--primary-glow);
              transform: translateY(-2px);
            }
            button.btn-cyan {
              border-color: var(--workplace);
              color: var(--workplace);
              background: rgba(0, 240, 255, 0.15);
            }
            button.btn-cyan:hover {
              background: var(--workplace);
              color: #000;
              box-shadow: 0 0 18px var(--workplace-glow);
            }
            button.btn-secondary {
              border-color: rgba(255,255,255,0.3);
              color: var(--text-dim);
              background: rgba(255,255,255,0.06);
            }
            button.btn-secondary:hover {
              border-color: #fff;
              color: #fff;
              background: rgba(255,255,255,0.2);
            }

            .modal-overlay {
              position: fixed;
              top: 0; left: 0; right: 0; bottom: 0;
              background: rgba(0, 0, 0, 0.85);
              z-index: 200;
              display: none;
              align-items: center;
              justify-content: center;
              backdrop-filter: blur(12px);
            }
            .modal-overlay.active { display: flex; }
            .modal-card {
              background: #0d121d;
              border: 1px solid var(--panel-border);
              width: 90%;
              max-width: 650px;
              max-height: 80vh;
              border-radius: 12px;
              display: flex;
              flex-direction: column;
              box-shadow: 0 15px 45px rgba(0,0,0,0.8);
              overflow: hidden;
            }
            .modal-header {
              padding: 16px 24px;
              border-bottom: 1px solid rgba(255,255,255,0.1);
              display: flex;
              justify-content: space-between;
              align-items: center;
            }
            .modal-header h3 { margin: 0; color: #fff; font-size: 18px; letter-spacing: 2px; }
            .modal-body {
              padding: 20px 24px;
              overflow-y: auto;
              flex: 1;
            }
            .search-input {
              width: 100%;
              background: rgba(255,255,255,0.07);
              border: 1px solid rgba(255,255,255,0.2);
              color: #fff;
              padding: 12px 16px;
              border-radius: 6px;
              font-size: 14px;
              margin-bottom: 16px;
              outline: none;
            }
            .search-input:focus { border-color: var(--workplace); }
            .app-grid {
              display: grid;
              grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
              gap: 10px;
            }
            .app-item {
              background: rgba(255,255,255,0.04);
              border: 1px solid rgba(255,255,255,0.1);
              padding: 10px 14px;
              border-radius: 6px;
              display: flex;
              align-items: center;
              justify-content: space-between;
              cursor: pointer;
              transition: all 0.2s;
            }
            .app-item:hover {
              background: rgba(0, 240, 255, 0.15);
              border-color: var(--workplace);
              transform: translateY(-1px);
            }
            .app-item span {
              color: #fff;
              font-size: 13px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }

            video { display: none; }
          </style>
          <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
          <script src="https://cdn.jsdelivr.net/npm/@mediapipe/camera_utils/camera_utils.js" crossorigin="anonymous"></script>
          <script src="https://cdn.jsdelivr.net/npm/@mediapipe/hands/hands.js" crossorigin="anonymous"></script>
        </head>
        <body>
          <div id="hud-top">
            <div class="title-block">
              <h1 id="title">VISION CORE</h1>
              <div id="status-badge"><span class="pulse-dot"></span> <span id="status-text">NEURAL LINK ACTIVE</span></div>
              <div id="voice-log">MIC: CLICK 'ACTIVATE MIC' OR PRESS SPACEBAR</div>
              <div id="ai-response">READY FOR SYSTEM COMMANDS</div>
            </div>

            <div class="telemetry-panel">
              <div class="telemetry-row">
                <span>MAC STATUS</span>
                <span class="telemetry-val" id="mac-batt">BATTERY: ...</span>
              </div>
              <div class="telemetry-row">
                <span>VOLUME</span>
                <span class="telemetry-val" id="mac-vol">...</span>
              </div>
              <div class="telemetry-row">
                <span>INDEXED APPS</span>
                <span class="telemetry-val" id="app-count">SCANNING...</span>
              </div>
              <div class="telemetry-row">
                <span>VIVO PHONE (ADB)</span>
                <span class="telemetry-val" id="phone-status">CHECKING...</span>
              </div>
            </div>
          </div>

          <!-- Notification Overlay -->
          <div id="notification-banner">
            <div class="notif-header">
              <span class="notif-app-tag" id="notif-app">WHATSAPP</span>
              <span class="notif-sender" id="notif-sender">RAHUL</span>
            </div>
            <div class="notif-body" id="notif-text">"Hey bro, are you available for the team meeting?"</div>
            <div class="notif-prompt">🎙️ <span>SAY "REPLY SAYING [MESSAGE]" OR PICK A QUICK REPLY:</span></div>
            <div class="quick-replies">
              <div class="reply-chip" onclick="quickReply('I will be there in 10 minutes.')">I'll be there in 10 mins</div>
              <div class="reply-chip" onclick="quickReply('Sounds good, let\'s do it.')">Sounds good!</div>
              <div class="reply-chip" onclick="quickReply('Busy right now, will call later.')">Busy, call later</div>
              <div class="reply-chip" onclick="quickReply('Yes, absolutely.')">Yes, absolutely</div>
            </div>
            <div class="notif-actions">
              <button onclick="promptVoiceReply()">🎙️ SPEAK REPLY</button>
              <button class="btn-secondary" onclick="dismissNotif()">DISMISS</button>
            </div>
          </div>

          <!-- App Catalog Modal -->
          <div class="modal-overlay" id="app-modal">
            <div class="modal-card">
              <div class="modal-header">
                <h3>MAC APPLICATION DIRECTORY</h3>
                <button class="btn-secondary" style="padding: 6px 12px;" onclick="closeAppModal()">✕</button>
              </div>
              <div class="modal-body">
                <input type="text" id="app-search" class="search-input" placeholder="Search any installed application..." oninput="filterApps()">
                <div class="app-grid" id="app-grid-list"></div>
              </div>
            </div>
          </div>

          <!-- Contacts Modal -->
          <div class="modal-overlay" id="contact-modal">
            <div class="modal-card">
              <div class="modal-header">
                <h3>CONTACTS DIRECTORY</h3>
                <button class="btn-secondary" style="padding: 6px 12px;" onclick="closeContactModal()">✕</button>
              </div>
              <div class="modal-body">
                <div style="display: flex; gap: 8px; margin-bottom: 16px;">
                  <input type="text" id="new-contact-name" class="search-input" style="margin-bottom:0;" placeholder="Name (e.g. Rahul)">
                  <input type="text" id="new-contact-phone" class="search-input" style="margin-bottom:0;" placeholder="Phone (e.g. 919876543210)">
                  <button onclick="saveNewContact()">ADD</button>
                </div>
                <div id="contact-list" style="display:flex; flex-direction:column; gap:8px;"></div>
              </div>
            </div>
          </div>

          <!-- Bottom Control Bar -->
          <div id="controls">
            <div class="btn-group">
              <button id="mic-btn" onclick="toggleVoice()">🎙️ ACTIVATE MIC (SPACEBAR)</button>
              <button class="btn-secondary" style="border-color:#ff4444; color:#ff4444;" onclick="silenceVision()">⏹️ SILENCE (ESC)</button>
              <button class="btn-secondary" style="border-color:#00ff88; color:#00ff88;" onclick="promptVoiceCall()">📞 CALL</button>
              <button class="btn-cyan" onclick="toggleMode()">🔄 SWITCH MODE</button>
              <button class="btn-secondary" onclick="simulateTestNotification()">🔔 TEST NOTIFICATION</button>
            </div>
            <div class="btn-group">
              <button class="btn-secondary" onclick="openAppModal()">💻 ALL APPS</button>
              <button class="btn-secondary" onclick="openContactModal()">👥 CONTACTS</button>
            </div>
          </div>

          <video id="webcam" autoplay playsinline></video>

          <script>
            // Audio Chime Generator
            const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
            function playBeep(freq = 880, type = 'sine', duration = 0.15) {
              if (audioCtx.state === 'suspended') audioCtx.resume();
              const osc = audioCtx.createOscillator();
              const gain = audioCtx.createGain();
              osc.type = type;
              osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
              gain.gain.setValueAtTime(0.15, audioCtx.currentTime);
              gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + duration);
              osc.connect(gain);
              gain.connect(audioCtx.destination);
              osc.start();
              osc.stop(audioCtx.currentTime + duration);
            }
            function playJarvisChime() {
              playBeep(440, 'triangle', 0.1);
              setTimeout(() => playBeep(880, 'sine', 0.2), 100);
            }
            function playAlertPing() {
              playBeep(987.77, 'sawtooth', 0.12);
              setTimeout(() => playBeep(1318.51, 'sine', 0.25), 120);
            }

            // --- 3D PARTICLE HOLOGRAPHIC ENGINE ---
            const scene = new THREE.Scene();
            const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
            const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
            renderer.setSize(window.innerWidth, window.innerHeight);
            renderer.setPixelRatio(window.devicePixelRatio);
            document.body.appendChild(renderer.domElement);

            const particleCount = 8000;
            const geometry = new THREE.BufferGeometry();
            const positions = new Float32Array(particleCount * 3);
            const sphereTargets = new Float32Array(particleCount * 3);
            const armorTargets = new Float32Array(particleCount * 3);
            const colors = new Float32Array(particleCount * 3);

            for (let i = 0; i < particleCount; i++) {
              const r = 2.4 * Math.cbrt(Math.random());
              const theta = Math.random() * 2 * Math.PI;
              const phi = Math.acos(2 * Math.random() - 1);
              sphereTargets[i * 3] = r * Math.sin(phi) * Math.cos(theta);
              sphereTargets[i * 3 + 1] = r * Math.sin(phi) * Math.sin(theta);
              sphereTargets[i * 3 + 2] = r * Math.cos(phi);

              const section = Math.random();
              if (section < 0.2) {
                armorTargets[i*3] = (Math.random() - 0.5) * 0.8; armorTargets[i*3+1] = 1.8 + Math.random() * 0.8; armorTargets[i*3+2] = (Math.random() - 0.5) * 0.8;
              } else if (section < 0.6) {
                armorTargets[i*3] = (Math.random() - 0.5) * 1.8; armorTargets[i*3+1] = (Math.random() - 0.2) * 2.0; armorTargets[i*3+2] = (Math.random() - 0.5) * 1.0;
              } else if (section < 0.8) {
                const side = Math.random() > 0.5 ? 1 : -1;
                armorTargets[i*3] = side * (1.1 + Math.random() * 2.2); armorTargets[i*3+1] = 0.5 + (Math.random() - 0.5) * 1.5; armorTargets[i*3+2] = (Math.random() - 0.5) * 0.6;
              } else {
                const leg = Math.random() > 0.5 ? 0.6 : -0.6;
                armorTargets[i*3] = leg + (Math.random() - 0.5) * 0.6; armorTargets[i*3+1] = -0.5 - Math.random() * 2.2; armorTargets[i*3+2] = (Math.random() - 0.5) * 0.6;
              }

              positions[i * 3] = sphereTargets[i * 3];
              positions[i * 3 + 1] = sphereTargets[i * 3 + 1];
              positions[i * 3 + 2] = sphereTargets[i * 3 + 2];
              colors[i * 3] = 1.0; colors[i * 3 + 1] = 0.16; colors[i * 3 + 2] = 0.16;
            }

            geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
            geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
            const material = new THREE.PointsMaterial({ size: 0.038, vertexColors: true, transparent: true, opacity: 0.85, blending: THREE.AdditiveBlending });
            const particleSystem = new THREE.Points(geometry, material);
            scene.add(particleSystem);
            camera.position.z = 6.2;

            let isWorkplaceMode = false;
            let targetScale = 1, targetRotX = 0, targetRotY = 0;

            function setMode(workplace) {
              isWorkplaceMode = workplace;
              const colAttr = geometry.attributes.color.array;
              for (let i = 0; i < particleCount; i++) {
                if (isWorkplaceMode) { colAttr[i*3] = 0.0; colAttr[i*3+1] = 0.94; colAttr[i*3+2] = 1.0; }
                else { colAttr[i*3] = 1.0; colAttr[i*3+1] = 0.16; colAttr[i*3+2] = 0.16; }
              }
              geometry.attributes.color.needsUpdate = true;
              const title = document.getElementById('title');
              title.innerText = isWorkplaceMode ? "WORKPLACE COMMAND" : "VISION CORE";
              title.style.color = isWorkplaceMode ? "var(--workplace)" : "var(--primary)";
              title.style.textShadow = isWorkplaceMode ? "0 0 20px var(--workplace-glow)" : "0 0 20px var(--primary-glow)";
              playJarvisChime();
            }
            function toggleMode() { setMode(!isWorkplaceMode); }

            // --- HAND TRACKING (MEDIAPIPE) ---
            const videoElement = document.getElementById('webcam');
            const statusText = document.getElementById('status-text');
            try {
              const hands = new Hands({ locateFile: (file) => `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}` });
              hands.setOptions({ maxNumHands: 2, modelComplexity: 1, minDetectionConfidence: 0.6, minTrackingConfidence: 0.6 });
              hands.onResults((results) => {
                if (results.multiHandLandmarks && results.multiHandLandmarks.length > 0) {
                  statusText.innerText = `GESTURE LOCKED (${results.multiHandLandmarks.length})`;
                  const h1 = results.multiHandLandmarks[0];
                  targetRotY = (h1[9].x - 0.5) * 5.5; targetRotX = (h1[9].y - 0.5) * 5.5;
                  if (results.multiHandLandmarks.length === 2) {
                    const h2 = results.multiHandLandmarks[1];
                    targetScale = Math.max(0.4, Math.hypot(h1[9].x - h2[9].x, h1[9].y - h2[9].y) * 3.5);
                  } else {
                    targetScale = Math.max(0.4, Math.hypot(h1[4].x - h1[8].x, h1[4].y - h1[8].y) * 6);
                  }
                } else {
                  statusText.innerText = "NEURAL LINK ACTIVE";
                  targetRotY += 0.005;
                }
              });
              const cam = new Camera(videoElement, { onFrame: async () => { await hands.send({ image: videoElement }); }, width: 640, height: 480 });
              cam.start();
            } catch(e) {}

            function animate() {
              requestAnimationFrame(animate);
              const posAttr = geometry.attributes.position.array;
              const targetArr = isWorkplaceMode ? armorTargets : sphereTargets;
              for (let i = 0; i < particleCount * 3; i++) posAttr[i] += (targetArr[i] - posAttr[i]) * 0.06;
              geometry.attributes.position.needsUpdate = true;
              particleSystem.rotation.y += (targetRotY - particleSystem.rotation.y) * 0.1;
              particleSystem.rotation.x += (targetRotX - particleSystem.rotation.x) * 0.1;
              const s = particleSystem.scale.x + (targetScale - particleSystem.scale.x) * 0.1;
              particleSystem.scale.set(s, s, s);
              renderer.render(scene, camera);
            }
            animate();

            window.addEventListener('resize', () => {
              camera.aspect = window.innerWidth / window.innerHeight;
              camera.updateProjectionMatrix();
              renderer.setSize(window.innerWidth, window.innerHeight);
            });

            // --- CONTINUOUS SIRI-STYLE VOICE ENGINE ---
            const voiceLog = document.getElementById('voice-log');
            const aiResponse = document.getElementById('ai-response');
            const micBtn = document.getElementById('mic-btn');
            const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
            let recognition;
            let userMicActive = false;
            let isProcessing = false;
            let lastSentText = "";
            let lastSentTime = 0;

            function initVoice() {
              if (!SpeechRecognition) {
                voiceLog.innerText = "Speech API not supported in this browser. Use Chrome/Safari.";
                return;
              }
              recognition = new SpeechRecognition();
              recognition.continuous = true;
              recognition.interimResults = true;
              recognition.lang = 'en-IN';

              recognition.onstart = () => {
                userMicActive = true;
                micBtn.innerHTML = "🔴 LISTENING... (SIRI ACTIVE)";
                micBtn.style.borderColor = "var(--accent-green)";
                micBtn.style.color = "var(--accent-green)";
                voiceLog.innerText = "🎙️ LISTENING: SPEAK ANY COMMAND (e.g. 'Open WhatsApp', 'Call Rahul')...";
                playBeep(660, 'sine', 0.1);
              };

              recognition.onresult = (event) => {
                if (isProcessing) return;
                let interim = '', final = '';
                for (let i = event.resultIndex; i < event.results.length; ++i) {
                  if (event.results[i].isFinal) final += event.results[i][0].transcript;
                  else interim += event.results[i][0].transcript;
                }
                if (interim) voiceLog.innerText = `HEARING: "${interim.trim()}"`;
                let phrase = (final || interim).trim();
                if (!phrase || phrase.length < 2) return;
                if (final) handleVoiceCommand(phrase);
              };

              recognition.onend = () => {
                if (userMicActive && !isProcessing) {
                  setTimeout(() => { if (userMicActive && !isProcessing) try { recognition.start(); } catch(e){} }, 300);
                } else if (!isProcessing) {
                  micBtn.innerHTML = "🎙️ ACTIVATE MIC (SPACEBAR)";
                  micBtn.style.borderColor = "var(--primary)";
                  micBtn.style.color = "var(--primary)";
                }
              };

              recognition.onerror = () => {
                if (userMicActive && !isProcessing) {
                  setTimeout(() => { if (userMicActive && !isProcessing) try { recognition.start(); } catch(e){} }, 400);
                }
              };
            }

            function handleVoiceCommand(text) {
              const now = Date.now();
              if (text.toLowerCase() === lastSentText.toLowerCase() && (now - lastSentTime < 3000)) return;
              lastSentText = text;
              lastSentTime = now;

              isProcessing = true;
              voiceLog.innerText = `YOU: "${text}"`;
              micBtn.innerHTML = "⏳ EXECUTING...";
              micBtn.style.borderColor = "var(--workplace)";
              micBtn.style.color = "var(--workplace)";
              playBeep(880, 'sine', 0.08);

              // Pause mic so it never hears itself speak
              try { recognition.abort(); } catch(e) {}

              const lower = text.toLowerCase();
              if (lower.includes("workplace") || lower.includes("workspace")) setMode(true);
              else if (lower.includes("core mode")) setMode(false);

              sendVoiceCommand(text);
            }

            function toggleVoice() {
              if (!recognition) initVoice();
              if (!recognition) return;
              if (userMicActive) {
                userMicActive = false; isProcessing = false;
                try { recognition.abort(); } catch(e) {}
                micBtn.innerHTML = "🎙️ ACTIVATE MIC (SPACEBAR)";
                micBtn.style.borderColor = "var(--primary)";
                micBtn.style.color = "var(--primary)";
                voiceLog.innerText = "MIC OFF";
              } else {
                userMicActive = true; isProcessing = false;
                try { recognition.start(); } catch(e) {}
              }
            }

            function silenceVision() {
              fetch('/api/stop', { method: 'POST' });
              if ('speechSynthesis' in window) window.speechSynthesis.cancel();
              aiResponse.innerText = "VISION: Silenced.";
              isProcessing = false;
              if (userMicActive && recognition) try { recognition.start(); } catch(e) {}
              playBeep(440, 'triangle', 0.1);
            }

            function promptVoiceCall() {
              const target = prompt("Enter contact name (Rahul, Mom, Dad, Boss) or phone number to call:");
              if (target && target.trim()) sendVoiceCommand("call " + target.trim());
            }

            function speakInBrowser(text) {
              if (!text || !('speechSynthesis' in window)) return;
              try {
                window.speechSynthesis.cancel();
                const utter = new SpeechSynthesisUtterance(text);
                utter.rate = 1.05; utter.lang = 'en-US';
                window.speechSynthesis.speak(utter);
              } catch(e) {}
            }

            function sendVoiceCommand(text) {
              aiResponse.innerText = "PROCESSING: " + text + "...";
              fetch('/command?text=' + encodeURIComponent(text))
                .then(r => r.json())
                .then(data => {
                  const spoken = data.spoken || "Command executed.";
                  aiResponse.innerText = "VISION: " + spoken;
                  voiceLog.innerText = `LAST: "${text}"`;
                  speakInBrowser(spoken);
                  fetchStatus();
                  setTimeout(() => {
                    isProcessing = false;
                    if (userMicActive && recognition) {
                      try { recognition.start(); } catch(e) {}
                      micBtn.innerHTML = "🔴 LISTENING... (SIRI ACTIVE)";
                      micBtn.style.borderColor = "var(--accent-green)";
                      micBtn.style.color = "var(--accent-green)";
                      voiceLog.innerText = "🎙️ LISTENING: SPEAK ANY COMMAND...";
                    } else {
                      micBtn.innerHTML = "🎙️ ACTIVATE MIC (SPACEBAR)";
                      micBtn.style.borderColor = "var(--primary)";
                      micBtn.style.color = "var(--primary)";
                    }
                  }, 2400);
                })
                .catch(() => {
                  executeClientSide(text);
                });
            }

            function executeClientSide(text) {
              const lower = text.toLowerCase().trim();
              let spoken = "";
              const contacts = {
                "rahul": "919876543210",
                "mom": "919876543211",
                "dad": "919876543212",
                "boss": "919876543213",
                "ragu": "9876543210"
              };

              if (lower.includes("hello") || lower.includes("hi") || lower.includes("hey")) {
                spoken = "Hello sir. Vision is online and ready.";
              } else if (lower.startsWith("call ") || lower.includes("call")) {
                let target = lower.replace(/.*call\s+/i, '').replace(/on phone|now|please/gi, '').trim();
                let num = contacts[target] || target.replace(/[^0-9+]/g, '');
                for (const [k, v] of Object.entries(contacts)) {
                  if (target.includes(k)) { num = v; target = k; break; }
                }
                if (num && num.length >= 3) {
                  spoken = "Calling " + target + " now.";
                  window.open("tel:" + num, "_blank");
                } else {
                  spoken = "Could not find a phone number for " + target + ".";
                }
              } else if (lower.includes("whatsapp")) {
                spoken = "Opening WhatsApp.";
                window.open("https://web.whatsapp.com", "_blank");
              } else if (lower.includes("youtube")) {
                spoken = "Opening YouTube.";
                window.open("https://www.youtube.com", "_blank");
              } else if (lower.includes("google") || lower.includes("chrome")) {
                spoken = "Opening Google.";
                window.open("https://www.google.com", "_blank");
              } else if (lower.includes("workplace") || lower.includes("workspace")) {
                setMode(true);
                spoken = "Workplace mode engaged.";
              } else if (lower.includes("core mode")) {
                setMode(false);
                spoken = "Vision Core restored.";
              } else {
                spoken = "Processing: " + text;
                window.open("https://www.google.com/search?q=" + encodeURIComponent(text), "_blank");
              }

              aiResponse.innerText = "VISION: " + spoken;
              voiceLog.innerText = `LAST: "${text}"`;
              speakInBrowser(spoken);

              setTimeout(() => {
                isProcessing = false;
                if (userMicActive && recognition) {
                  try { recognition.start(); } catch(e) {}
                  micBtn.innerHTML = "🔴 LISTENING... (SIRI ACTIVE)";
                  micBtn.style.borderColor = "var(--accent-green)";
                  micBtn.style.color = "var(--accent-green)";
                  voiceLog.innerText = "🎙️ LISTENING: SPEAK ANY COMMAND...";
                } else {
                  micBtn.innerHTML = "🎙️ ACTIVATE MIC (SPACEBAR)";
                  micBtn.style.borderColor = "var(--primary)";
                  micBtn.style.color = "var(--primary)";
                }
              }, 2400);
            }

            // --- NOTIFICATION BANNER & QUICK REPLIES ---
            let currentPendingId = null;
            function checkNotifications(data) {
              const banner = document.getElementById('notification-banner');
              if (data.pendingNotification) {
                const notif = data.pendingNotification;
                if (notif.id !== currentPendingId) {
                  currentPendingId = notif.id;
                  document.getElementById('notif-app').innerText = notif.app.toUpperCase();
                  document.getElementById('notif-sender').innerText = notif.sender.toUpperCase();
                  document.getElementById('notif-text').innerText = `"${notif.text}"`;
                  banner.classList.add('active');
                  playAlertPing();
                }
              } else {
                if (currentPendingId !== null) { banner.classList.remove('active'); currentPendingId = null; }
              }
            }
            function quickReply(text) { sendVoiceCommand("reply saying " + text); }
            function promptVoiceReply() { if (!userMicActive) toggleVoice(); voiceLog.innerText = "🎙️ SPEAK YOUR REPLY NOW..."; }
            function dismissNotif() {
              sendVoiceCommand("dismiss");
              document.getElementById('notification-banner').classList.remove('active');
              currentPendingId = null;
            }
            function simulateTestNotification() { fetch('/api/simulate-notification', { method: 'POST' }).then(() => fetchStatus()); }

            // --- TELEMETRY ---
            function fetchStatus() {
              fetch('/api/status').then(r => r.json()).then(data => {
                if (data.battery) document.getElementById('mac-batt').innerText = data.battery;
                if (data.volume !== undefined) document.getElementById('mac-vol').innerText = data.volume + '%';
                if (data.appCount !== undefined) document.getElementById('app-count').innerText = data.appCount + ' DETECTED';
                if (data.phoneStatus) document.getElementById('phone-status').innerText = data.phoneStatus;
                checkNotifications(data);
              }).catch(() => {});
            }
            setInterval(fetchStatus, 2500);
            fetchStatus();

            // --- APP MODAL ---
            let allInstalledApps = [];
            function openAppModal() {
              document.getElementById('app-modal').classList.add('active');
              fetch('/api/apps').then(r => r.json()).then(apps => { allInstalledApps = apps; renderAppGrid(apps); });
            }
            function closeAppModal() { document.getElementById('app-modal').classList.remove('active'); }
            function renderAppGrid(list) {
              const container = document.getElementById('app-grid-list');
              container.innerHTML = '';
              list.slice(0, 100).forEach(app => {
                const item = document.createElement('div');
                item.className = 'app-item';
                item.innerHTML = `<span>${app.displayName}</span>`;
                item.onclick = () => { sendVoiceCommand("open " + app.displayName); closeAppModal(); };
                container.appendChild(item);
              });
            }
            function filterApps() {
              const q = document.getElementById('app-search').value.toLowerCase();
              renderAppGrid(allInstalledApps.filter(a => a.displayName.toLowerCase().includes(q)));
            }

            // --- CONTACTS MODAL ---
            function openContactModal() { document.getElementById('contact-modal').classList.add('active'); loadContacts(); }
            function closeContactModal() { document.getElementById('contact-modal').classList.remove('active'); }
            function loadContacts() {
              fetch('/api/contacts').then(r => r.json()).then(data => {
                const list = document.getElementById('contact-list');
                list.innerHTML = '';
                for (let name in data) {
                  const row = document.createElement('div');
                  row.className = 'app-item';
                  row.innerHTML = `<span style="font-weight:bold; color:var(--workplace);">${name.toUpperCase()}</span> <span>${data[name]}</span>`;
                  list.appendChild(row);
                }
              });
            }
            function saveNewContact() {
              const name = document.getElementById('new-contact-name').value.trim();
              const phone = document.getElementById('new-contact-phone').value.trim();
              if (!name || !phone) return;
              fetch('/api/contacts?name=' + encodeURIComponent(name) + '&phone=' + encodeURIComponent(phone), { method: 'POST' })
                .then(() => {
                  document.getElementById('new-contact-name').value = '';
                  document.getElementById('new-contact-phone').value = '';
                  loadContacts();
                });
            }

            window.addEventListener('load', () => { initVoice(); });
            window.addEventListener('keydown', (e) => {
              if (e.code === 'Escape') { e.preventDefault(); silenceVision(); }
              else if (e.code === 'Space' && e.target.tagName !== 'INPUT') { e.preventDefault(); toggleVoice(); }
            });
          </script>
        </body>
        </html>
        """;

    // =========================================================================
    // 2. TEXT-TO-SPEECH (STRICT SINGLE-SPEAKER MUTEX)
    // =========================================================================
    public static void stopSpeaking() {
        synchronized (speakLock) {
            if (currentSayProcess != null && currentSayProcess.isAlive()) {
                try {
                    currentSayProcess.destroyForcibly();
                    currentSayProcess.waitFor(200, TimeUnit.MILLISECONDS);
                } catch (Exception ignored) {}
                currentSayProcess = null;
            }
            isSpeaking = false;
        }
    }

    public static boolean isSpeaking() {
        return isSpeaking;
    }

    public static void speak(String text) {
        if (text == null || text.trim().isEmpty()) return;
        lastSpoken = text;
        System.out.println("[VISION]: " + text);

        new Thread(() -> {
            synchronized (speakLock) {
                if (currentSayProcess != null && currentSayProcess.isAlive()) {
                    try {
                        currentSayProcess.destroyForcibly();
                        currentSayProcess.waitFor(200, TimeUnit.MILLISECONDS);
                    } catch (Exception ignored) {}
                    currentSayProcess = null;
                }
                try {
                    isSpeaking = true;
                    currentSayProcess = new ProcessBuilder("say", text).start();
                    currentSayProcess.waitFor();
                } catch (Exception e) {
                    System.err.println("[VISION] Speech error: " + e.getMessage());
                } finally {
                    isSpeaking = false;
                    currentSayProcess = null;
                }
            }
        }, "TTS-Thread").start();
    }

    // =========================================================================
    // 3. MAIN SERVER SETUP
    // =========================================================================
    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("   VISION AI 2.0 - ALL-IN-ONE SYSTEM COMMANDER   ");
        System.out.println("==================================================");

        ContactManager.load();
        AppManager.initialize();
        AdbManager.detectAdbPath();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", (HttpExchange exchange) -> {
            byte[] html = HTML_CONTENT.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(html); }
        });

        server.createContext("/command", (HttpExchange exchange) -> {
            String query = exchange.getRequestURI().getQuery();
            String resultAction = "NO_OP";
            if (query != null && query.contains("text=")) {
                String cmd = extractParam(query, "text");
                if (cmd != null) cmd = cmd.trim();

                if (cmd != null && (cmd.equalsIgnoreCase("stop") || cmd.equalsIgnoreCase("silence") ||
                    cmd.equalsIgnoreCase("be quiet") || cmd.equalsIgnoreCase("shut up") || cmd.equalsIgnoreCase("cancel speech"))) {
                    stopSpeaking();
                    sendJson(exchange, 200, "{\"status\":\"OK\",\"action\":\"SILENCED\",\"spoken\":\"\",\"isSpeaking\":false}");
                    return;
                }

                long now = System.currentTimeMillis();
                boolean isDuplicate = (cmd != null) && (
                    cmd.equalsIgnoreCase(lastCommand) && (now - lastCommandTime < 3000) ||
                    (now - lastCommandTime < 2500 && (cmd.contains(lastCommand) || lastCommand.contains(cmd)))
                );

                if (isDuplicate) {
                    resultAction = "IGNORED_DUPLICATE";
                } else if (cmd != null && !cmd.isEmpty()) {
                    lastCommand = cmd;
                    lastCommandTime = now;
                    resultAction = Brain.processCommand(cmd);
                }
            }
            String spokenVal = (resultAction.startsWith("IGNORED") || resultAction.equals("NO_OP")) ? "" : lastSpoken;
            String json = String.format("{\"status\":\"OK\",\"action\":\"%s\",\"spoken\":%s,\"isSpeaking\":%b}",
                    escapeJson(resultAction), quoteJson(spokenVal), isSpeaking());
            sendJson(exchange, 200, json);
        });

        server.createContext("/api/stop", (HttpExchange exchange) -> {
            stopSpeaking();
            sendJson(exchange, 200, "{\"status\":\"OK\",\"message\":\"Speech silenced.\"}");
        });

        server.createContext("/api/status", (HttpExchange exchange) -> {
            StringBuilder json = new StringBuilder("{");
            json.append("\"battery\":").append(quoteJson(MacAutomation.getBatteryStatus())).append(",");
            json.append("\"volume\":").append(MacAutomation.getVolume()).append(",");
            json.append("\"appCount\":").append(AppManager.getAppCount()).append(",");
            json.append("\"isSpeaking\":").append(isSpeaking()).append(",");

            String phoneStatus = AdbManager.hasConnectedDevice()
                    ? "CONNECTED (" + String.join(", ", AdbManager.getConnectedDevices()) + ")"
                    : (AdbManager.isAvailable() ? "NO DEVICE PAIRED" : "ADB NOT INSTALLED");
            json.append("\"phoneStatus\":").append(quoteJson(phoneStatus)).append(",");

            NotificationManager.NotificationItem pending = NotificationManager.getPendingNotification();
            if (pending != null) {
                json.append("\"pendingNotification\":{")
                    .append("\"id\":").append(quoteJson(pending.id)).append(",")
                    .append("\"sender\":").append(quoteJson(pending.sender)).append(",")
                    .append("\"app\":").append(quoteJson(pending.app)).append(",")
                    .append("\"text\":").append(quoteJson(pending.text)).append(",")
                    .append("\"phone\":").append(quoteJson(pending.phone))
                    .append("}");
            } else {
                json.append("\"pendingNotification\":null");
            }
            json.append("}");
            sendJson(exchange, 200, json.toString());
        });

        server.createContext("/api/notify", (HttpExchange exchange) -> {
            String sender = "System", app = "Notification", text = "New alert received.", phone = "";
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                    String b = body.toString();
                    if (b.startsWith("{")) {
                        sender = extractJsonField(b, "sender", sender);
                        app = extractJsonField(b, "app", app);
                        text = extractJsonField(b, "text", text);
                        phone = extractJsonField(b, "phone", phone);
                    }
                }
            } else {
                String q = exchange.getRequestURI().getQuery();
                if (q != null) {
                    sender = extractParam(q, "sender", sender);
                    app = extractParam(q, "app", app);
                    text = extractParam(q, "text", text);
                    phone = extractParam(q, "phone", phone);
                }
            }
            NotificationManager.NotificationItem notif = NotificationManager.receiveNotification(sender, app, text, phone);
            sendJson(exchange, 200, "{\"status\":\"RECEIVED\",\"id\":" + quoteJson(notif.id) + "}");
        });

        server.createContext("/api/simulate-notification", (HttpExchange exchange) -> {
            String[] testSenders = {"Rahul", "Mom", "Boss"};
            String[] testMessages = {
                "Hey bro, are you coming to the office today?",
                "Beta, don't forget to have lunch on time!",
                "Can you check the latest update on the project when you're free?"
            };
            int idx = (int)(Math.random() * testSenders.length);
            NotificationManager.receiveNotification(testSenders[idx], "WhatsApp", testMessages[idx], "");
            sendJson(exchange, 200, "{\"status\":\"SIMULATED\"}");
        });

        server.createContext("/api/apps", (HttpExchange exchange) -> {
            List<AppManager.AppEntry> apps = AppManager.getAllApps();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < apps.size(); i++) {
                AppManager.AppEntry a = apps.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"displayName\":").append(quoteJson(a.displayName)).append(",\"path\":").append(quoteJson(a.path)).append("}");
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        });

        server.createContext("/api/contacts", (HttpExchange exchange) -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String q = exchange.getRequestURI().getQuery();
                String name = extractParam(q, "name");
                String phone = extractParam(q, "phone");
                if (name != null && phone != null) ContactManager.setContact(name, phone);
            }
            Map<String, String> contacts = ContactManager.getAll();
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, String> entry : contacts.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append(quoteJson(entry.getKey())).append(":").append(quoteJson(entry.getValue()));
            }
            sb.append("}");
            sendJson(exchange, 200, sb.toString());
        });

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("[VISION]: Server running at http://localhost:" + PORT);
        try { Desktop.getDesktop().browse(new URI("http://localhost:" + PORT)); } catch (Exception ignored) {}
        speak("Good day sir. Vision online. Computer access and notification protocols initialized.");
    }

    // =========================================================================
    // 4. APPLICATION MANAGER (SPOTLIGHT INDEXER & RESOLVER)
    // =========================================================================
    public static class AppManager {
        public static class AppEntry {
            public final String displayName;
            public final String path;
            public final String normalizedName;
            public AppEntry(String displayName, String path) {
                this.displayName = displayName;
                this.path = path;
                this.normalizedName = normalize(displayName);
            }
        }

        private static final Map<String, AppEntry> indexedApps = new ConcurrentHashMap<>();
        private static final Map<String, String> aliasMap = new HashMap<>();
        private static final Map<String, String> webServices = new HashMap<>();

        static {
            aliasMap.put("chrome", "Google Chrome");
            aliasMap.put("google chrome", "Google Chrome");
            aliasMap.put("browser", "Google Chrome");
            aliasMap.put("code", "Visual Studio Code");
            aliasMap.put("vscode", "Visual Studio Code");
            aliasMap.put("vs code", "Visual Studio Code");
            aliasMap.put("calc", "Calculator");
            aliasMap.put("calculator", "Calculator");
            aliasMap.put("settings", "System Settings");
            aliasMap.put("system settings", "System Settings");
            aliasMap.put("preferences", "System Settings");
            aliasMap.put("terminal", "Terminal");
            aliasMap.put("term", "Terminal");
            aliasMap.put("iterm", "iTerm");
            aliasMap.put("whatsapp", "WhatsApp");
            aliasMap.put("wa", "WhatsApp");
            aliasMap.put("music", "Music");
            aliasMap.put("spotify", "Spotify");
            aliasMap.put("notes", "Notes");
            aliasMap.put("messages", "Messages");
            aliasMap.put("mail", "Mail");
            aliasMap.put("photos", "Photos");
            aliasMap.put("safari", "Safari");
            aliasMap.put("finder", "Finder");

            webServices.put("whatsapp", "https://web.whatsapp.com");
            webServices.put("youtube", "https://www.youtube.com");
            webServices.put("netflix", "https://www.netflix.com");
            webServices.put("github", "https://www.github.com");
            webServices.put("reddit", "https://www.reddit.com");
            webServices.put("twitter", "https://twitter.com");
            webServices.put("x", "https://x.com");
            webServices.put("instagram", "https://www.instagram.com");
            webServices.put("linkedin", "https://www.linkedin.com");
            webServices.put("chatgpt", "https://chatgpt.com");
        }

        public static void initialize() {
            new Thread(AppManager::scanApplications, "AppScanner-Thread").start();
        }

        public static synchronized void scanApplications() {
            Map<String, AppEntry> newMap = new HashMap<>();
            try {
                Process p = new ProcessBuilder("mdfind", "kMDItemContentType == 'com.apple.application-bundle'").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) addAppPath(line.trim(), newMap);
                }
                p.waitFor();
            } catch (Exception ignored) {}

            String home = System.getProperty("user.home");
            String[] appDirs = {"/Applications", "/System/Applications", "/System/Applications/Utilities", home + "/Applications", "/System/Library/CoreServices/Finder.app"};
            for (String dirPath : appDirs) {
                File dir = new File(dirPath);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().endsWith(".app")) addAppPath(f.getAbsolutePath(), newMap);
                        }
                    }
                }
            }
            indexedApps.clear();
            indexedApps.putAll(newMap);
            System.out.println("[AppManager] Successfully indexed " + indexedApps.size() + " applications.");
        }

        private static void addAppPath(String path, Map<String, AppEntry> map) {
            if (!path.endsWith(".app")) return;
            if (path.contains("/Frameworks/") || path.contains("/PrivateFrameworks/") ||
                path.contains("/Input Methods/") || path.contains("/Contents/") ||
                (path.contains("/Library/CoreServices/") && !path.contains("Finder.app"))) {
                return;
            }
            File file = new File(path);
            String cleanName = file.getName().substring(0, file.getName().length() - 4);
            map.put(cleanName.toLowerCase(), new AppEntry(cleanName, path));
        }

        public static String normalize(String s) {
            return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9]", "");
        }

        public static AppResolution resolve(String query) {
            if (query == null || query.trim().isEmpty()) return null;
            String raw = query.trim().toLowerCase();
            String norm = normalize(raw);

            if (aliasMap.containsKey(raw)) {
                String target = aliasMap.get(raw);
                for (AppEntry e : indexedApps.values()) {
                    if (e.displayName.equalsIgnoreCase(target) || e.normalizedName.equals(normalize(target))) return new AppResolution(e, null);
                }
            }
            if (indexedApps.containsKey(raw)) return new AppResolution(indexedApps.get(raw), null);
            for (AppEntry e : indexedApps.values()) {
                if (e.normalizedName.equals(norm) || e.normalizedName.startsWith(norm) || e.normalizedName.contains(norm)) {
                    return new AppResolution(e, null);
                }
            }
            if (webServices.containsKey(raw)) return new AppResolution(null, webServices.get(raw));
            return null;
        }

        public static boolean launchEntry(AppEntry entry) {
            if (entry == null) return false;
            System.out.println("[AppManager] Launching: " + entry.displayName + " [" + entry.path + "]");
            try {
                new ProcessBuilder("open", entry.path).start();
                return true;
            } catch (Exception e) {
                try {
                    new ProcessBuilder("open", "-a", entry.displayName).start();
                    return true;
                } catch (Exception ignored) { return false; }
            }
        }

        public static boolean openApp(String appQuery) {
            if (appQuery == null || appQuery.trim().isEmpty()) return false;
            String raw = appQuery.trim();
            if (raw.endsWith(".app") || new File(raw).exists()) {
                try {
                    new ProcessBuilder("open", raw).start();
                    return true;
                } catch (Exception ignored) {}
            }
            AppResolution res = resolve(raw);
            if (res == null) return false;
            if (res.appEntry != null) return launchEntry(res.appEntry);
            if (res.webUrl != null) {
                try {
                    Desktop.getDesktop().browse(new URI(res.webUrl));
                    return true;
                } catch (Exception e) {
                    try { new ProcessBuilder("open", res.webUrl).start(); return true; } catch (Exception ignored) {}
                }
            }
            return false;
        }

        public static boolean closeApp(String appQuery) {
            if (appQuery == null || appQuery.trim().isEmpty()) return false;
            AppResolution res = resolve(appQuery);
            String name = (res != null && res.appEntry != null) ? res.appEntry.displayName : appQuery.trim();
            try {
                new ProcessBuilder("osascript", "-e", "tell application \"" + name.replace("\"", "\\\"") + "\" to quit").start();
            } catch (Exception ignored) {}
            try {
                new ProcessBuilder("pkill", "-i", "-f", name).start();
            } catch (Exception ignored) {}
            return true;
        }

        public static List<String> getRunningApps() {
            List<String> list = new ArrayList<>();
            try {
                Process p = new ProcessBuilder("osascript", "-e", "tell application \"System Events\" to get name of every process whose background only is false").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null) for (String part : line.split(",")) if (!part.trim().isEmpty()) list.add(part.trim());
                }
            } catch (Exception ignored) {}
            return list;
        }

        public static List<AppEntry> getAllApps() {
            List<AppEntry> list = new ArrayList<>(indexedApps.values());
            list.sort(Comparator.comparing(a -> a.displayName.toLowerCase()));
            return list;
        }

        public static int getAppCount() { return indexedApps.size(); }

        public static class AppResolution {
            public final AppEntry appEntry;
            public final String webUrl;
            public AppResolution(AppEntry appEntry, String webUrl) {
                this.appEntry = appEntry;
                this.webUrl = webUrl;
            }
        }
    }

    // =========================================================================
    // 5. ANDROID MANAGER (ADB PHONE CALLS & PACKAGES)
    // =========================================================================
    public static class AdbManager {
        private static String adbPath = "adb";
        private static boolean adbAvailable = false;
        private static final Map<String, String> defaultAppPackages = new HashMap<>();

        static {
            defaultAppPackages.put("youtube", "com.google.android.youtube");
            defaultAppPackages.put("whatsapp", "com.whatsapp");
            defaultAppPackages.put("chrome", "com.android.chrome");
            defaultAppPackages.put("instagram", "com.instagram.android");
            defaultAppPackages.put("unstop", "com.unstop");
            defaultAppPackages.put("spotify", "com.spotify.music");
            defaultAppPackages.put("camera", "com.android.camera");
            detectAdbPath();
        }

        public static void detectAdbPath() {
            String home = System.getProperty("user.home");
            String[] possiblePaths = {"adb", "/opt/homebrew/bin/adb", "/usr/local/bin/adb", home + "/Library/Android/sdk/platform-tools/adb"};
            for (String p : possiblePaths) {
                try {
                    Process proc = new ProcessBuilder(p, "version").start();
                    if (proc.waitFor(1000, TimeUnit.MILLISECONDS) && proc.exitValue() == 0) {
                        adbPath = p; adbAvailable = true; return;
                    }
                } catch (Exception ignored) {}
            }
            adbAvailable = false;
        }

        public static boolean isAvailable() { return adbAvailable; }

        public static List<String> getConnectedDevices() {
            List<String> devices = new ArrayList<>();
            if (!adbAvailable) return devices;
            try {
                Process process = new ProcessBuilder(adbPath, "devices").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.endsWith("device") && !line.startsWith("List")) devices.add(line.split("\\t")[0]);
                    }
                }
                process.waitFor(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
            return devices;
        }

        public static boolean hasConnectedDevice() { return !getConnectedDevices().isEmpty(); }

        public static void runAdbOnAllDevices(String adbCommand) {
            List<String> devices = getConnectedDevices();
            if (devices.isEmpty()) return;
            for (String deviceId : devices) {
                new Thread(() -> {
                    try {
                        List<String> cmdList = new ArrayList<>();
                        cmdList.add(adbPath); cmdList.add("-s"); cmdList.add(deviceId);
                        for (String part : adbCommand.split(" ")) if (!part.isEmpty()) cmdList.add(part);
                        new ProcessBuilder(cmdList).start().waitFor(5, TimeUnit.SECONDS);
                    } catch (Exception ignored) {}
                }).start();
            }
        }

        public static void setBluetooth(boolean enable) { runAdbOnAllDevices("shell cmd bluetooth_manager " + (enable ? "enable" : "disable")); }
        public static void setWifi(boolean enable) { runAdbOnAllDevices("shell svc wifi " + (enable ? "enable" : "disable")); }

        public static void unlockPhone() {
            runAdbOnAllDevices("shell input keyevent 224");
            try { Thread.sleep(600); } catch (Exception ignored) {}
            runAdbOnAllDevices("shell input swipe 500 1500 500 200 300");
        }

        public static boolean callPhone(String number) {
            if (number == null || number.trim().isEmpty()) return false;
            String clean = number.replaceAll("[^0-9+]", "").trim();
            if (clean.isEmpty()) return false;
            runAdbOnAllDevices("shell input keyevent 224");
            try { Thread.sleep(300); } catch (Exception ignored) {}
            runAdbOnAllDevices("shell am start -a android.intent.action.CALL -d tel:" + clean);
            runAdbOnAllDevices("shell am start -a android.intent.action.DIAL -d tel:" + clean);
            return true;
        }

        public static String getBatteryStatus() {
            if (!hasConnectedDevice()) return "No Android mobile device is currently connected.";
            try {
                Process p = new ProcessBuilder(adbPath, "shell", "dumpsys", "battery").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().startsWith("level:")) {
                            return "Mobile battery is at " + line.trim().replace("level:", "").trim() + " percent.";
                        }
                    }
                }
            } catch (Exception ignored) {}
            return "Could not query mobile battery.";
        }

        public static boolean launchPhoneApp(String appName) {
            if (!hasConnectedDevice()) return false;
            String clean = appName.toLowerCase().trim();
            String pkg = defaultAppPackages.get(clean);
            if (pkg != null) {
                runAdbOnAllDevices("shell monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1");
                return true;
            }
            return false;
        }
    }

    // =========================================================================
    // 6. MAC AUTOMATION (VOLUME, MEDIA, CALLS)
    // =========================================================================
    public static class MacAutomation {
        public static void runBash(String cmd) {
            try { new ProcessBuilder("bash", "-c", cmd).start(); } catch (Exception ignored) {}
        }

        public static int getVolume() {
            try {
                Process p = new ProcessBuilder("osascript", "-e", "output volume of (get volume settings)").start();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    return Integer.parseInt(r.readLine().trim());
                }
            } catch (Exception e) { return 50; }
        }

        public static void setVolume(int level) {
            runBash("osascript -e 'set volume output volume " + Math.max(0, Math.min(100, level)) + "'");
        }

        public static void volumeUp() { setVolume(getVolume() + 10); }
        public static void volumeDown() { setVolume(getVolume() - 10); }
        public static void mute() { runBash("osascript -e 'set volume output muted true'"); }
        public static void unmute() { runBash("osascript -e 'set volume output muted false'"); }

        public static void playPause() {
            runBash("osascript -e 'tell application \"Music\" to playpause' 2>/dev/null || " +
                    "osascript -e 'tell application \"Spotify\" to playpause' 2>/dev/null || " +
                    "osascript -e 'tell application \"System Events\" to key code 16 using {command down}'");
        }

        public static void nextTrack() { runBash("osascript -e 'tell application \"Music\" to next track' 2>/dev/null"); }
        public static void previousTrack() { runBash("osascript -e 'tell application \"Music\" to previous track' 2>/dev/null"); }

        public static void lockScreen() { runBash("pmset displaysleepnow"); }
        public static void sleepMac() { runBash("osascript -e 'tell application \"System Events\" to sleep'"); }

        public static String takeScreenshot() {
            String home = System.getProperty("user.home");
            String path = home + "/Desktop/Screenshot_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png";
            runBash("screencapture -x \"" + path + "\"");
            return path;
        }

        public static void showDesktop() {
            runBash("osascript -e 'tell application \"System Events\" to set visible of every process whose visible is true to false'");
        }

        public static void emptyTrash() { runBash("osascript -e 'tell application \"Finder\" to empty trash'"); }

        public static String getBatteryStatus() {
            try {
                Process p = new ProcessBuilder("pmset", "-g", "batt").start();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        int idx = line.indexOf("%");
                        if (idx > 0) {
                            String sub = line.substring(Math.max(0, idx - 4), idx).trim();
                            String[] words = sub.split("[\\s\\t;,]");
                            return "Mac battery is at " + words[words.length - 1] + " percent.";
                        }
                    }
                }
            } catch (Exception ignored) {}
            return "Mac battery status unavailable.";
        }

        public static void searchGoogle(String query) {
            try { Desktop.getDesktop().browse(new URI("https://www.google.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8))); } catch (Exception ignored) {}
        }

        public static void searchYouTube(String query) {
            try { Desktop.getDesktop().browse(new URI("https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, StandardCharsets.UTF_8))); } catch (Exception ignored) {}
        }

        public static void callNumber(String number) {
            if (number == null || number.trim().isEmpty()) return;
            String clean = number.replaceAll("[^0-9+]", "").trim();
            if (!clean.isEmpty()) runBash("open \"tel:" + clean + "\"");
        }
    }

    // =========================================================================
    // 7. NOTIFICATION MANAGER
    // =========================================================================
    public static class NotificationManager {
        public static class NotificationItem {
            public final String id; public final String sender; public final String app;
            public final String text; public final String phone; public final long timestamp;
            public boolean replied = false; public String replyText = "";
            public NotificationItem(String sender, String app, String text, String phone) {
                this.id = "notif_" + System.currentTimeMillis();
                this.sender = sender != null ? sender.trim() : "Unknown";
                this.app = app != null ? app.trim() : "System";
                this.text = text != null ? text.trim() : "";
                this.phone = phone != null ? phone.trim() : "";
                this.timestamp = System.currentTimeMillis();
            }
        }

        private static volatile NotificationItem pendingNotification = null;

        public static NotificationItem getPendingNotification() { return pendingNotification; }
        public static boolean hasPendingNotification() { return pendingNotification != null && !pendingNotification.replied; }

        public static NotificationItem receiveNotification(String sender, String app, String text, String phone) {
            if (phone == null || phone.isEmpty()) {
                phone = ContactManager.getPhone(sender);
                if (phone == null) phone = "";
            }
            NotificationItem item = new NotificationItem(sender, app, text, phone);
            pendingNotification = item;
            VisionAgent.speak(String.format("Sir, incoming notification from %s on %s: \"%s\". Would you like to reply?", item.sender, item.app, item.text));
            return item;
        }

        public static boolean replyToPending(String userReply) {
            if (pendingNotification == null) {
                VisionAgent.speak("There is no active notification to reply to.");
                return false;
            }
            NotificationItem target = pendingNotification;
            String phone = target.phone.isEmpty() ? ContactManager.getPhone(target.sender) : target.phone;
            VisionAgent.speak(String.format("Replying to %s: \"%s\". Sending now.", target.sender, userReply));

            if (phone != null && !phone.isEmpty()) {
                sendWhatsAppMessage(phone, userReply);
            }
            target.replied = true;
            target.replyText = userReply;
            pendingNotification = null;
            return true;
        }

        public static void dismissPending() {
            if (pendingNotification != null) {
                pendingNotification = null;
                VisionAgent.speak("Notification dismissed.");
            }
        }

        public static boolean sendWhatsAppMessage(String phone, String message) {
            if (phone == null || phone.trim().isEmpty()) return false;
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            try {
                String encodedText = URLEncoder.encode(message, StandardCharsets.UTF_8);
                new ProcessBuilder("open", "whatsapp://send?phone=" + cleanPhone + "&text=" + encodedText).start();
                new Thread(() -> {
                    try {
                        Thread.sleep(1800);
                        new ProcessBuilder("osascript", "-e", "tell application \"System Events\" to tell process \"WhatsApp\" to key code 36").start();
                    } catch (Exception ignored) {}
                }).start();
                return true;
            } catch (Exception ignored) { return false; }
        }
    }

    // =========================================================================
    // 8. CONTACTS MANAGER
    // =========================================================================
    public static class ContactManager {
        private static final String FILE_PATH = "contacts.properties";
        private static final Map<String, String> contacts = new ConcurrentHashMap<>();

        static {
            contacts.put("rahul", "919876543210");
            contacts.put("mom", "919876543211");
            contacts.put("dad", "919876543212");
            contacts.put("boss", "919876543213");
            contacts.put("ragu", "9876543210");
        }

        public static synchronized void load() {
            File file = new File(FILE_PATH);
            if (!file.exists()) { save(); return; }
            Properties props = new Properties();
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
                for (String key : props.stringPropertyNames()) contacts.put(key.toLowerCase().trim(), props.getProperty(key).trim());
            } catch (Exception ignored) {}
        }

        public static synchronized void save() {
            Properties props = new Properties();
            for (Map.Entry<String, String> entry : contacts.entrySet()) props.setProperty(entry.getKey(), entry.getValue());
            try (OutputStream out = new FileOutputStream(FILE_PATH)) { props.store(out, "Vision Agent Contacts"); } catch (Exception ignored) {}
        }

        public static String getPhone(String name) {
            if (name == null) return null;
            String clean = name.toLowerCase().trim();
            if (contacts.containsKey(clean)) return contacts.get(clean);
            for (String key : contacts.keySet()) if (clean.contains(key) || key.contains(clean)) return contacts.get(key);
            return null;
        }

        public static void setContact(String name, String phone) {
            if (name != null && phone != null) {
                contacts.put(name.toLowerCase().trim(), phone.replaceAll("[^0-9+]", "").trim());
                save();
            }
        }

        public static Map<String, String> getAll() { return Collections.unmodifiableMap(new TreeMap<>(contacts)); }
    }

    // =========================================================================
    // 9. THE BRAIN (NATURAL LANGUAGE COMMAND PROCESSOR)
    // =========================================================================
    public static class Brain {
        public static String processCommand(String rawCmd) {
            if (rawCmd == null || rawCmd.trim().isEmpty()) return "NO_OP";
            String cmd = rawCmd.trim().toLowerCase();
            if (cmd.length() < 3 && !cmd.equals("no")) return "IGNORED_SHORT";
            if (cmd.contains("understood sir") || cmd.contains("vision online") || 
                cmd.contains("computer access") || cmd.contains("protocols ready") ||
                cmd.contains("good day sir") || cmd.equals("understo") || cmd.equals("unders") || cmd.equals("un")) {
                return "IGNORED_ECHO";
            }
            System.out.println("\n[Brain] Processing input: \"" + cmd + "\"");

            // A. Notification Reply
            if (NotificationManager.hasPendingNotification()) {
                if (cmd.equals("ignore") || cmd.equals("dismiss") || cmd.equals("cancel") || cmd.equals("no")) {
                    NotificationManager.dismissPending();
                    return "NOTIFICATION_DISMISSED";
                }
                String replyText = null;
                if (cmd.startsWith("reply saying ")) replyText = rawCmd.substring(13).trim();
                else if (cmd.startsWith("reply with ")) replyText = rawCmd.substring(11).trim();
                else if (cmd.startsWith("reply ")) replyText = rawCmd.substring(6).trim();
                else if (cmd.startsWith("tell him ") || cmd.startsWith("tell her ")) replyText = rawCmd.substring(9).trim();

                if (replyText != null && !replyText.isEmpty()) {
                    NotificationManager.replyToPending(replyText);
                    return "REPLIED_TO_NOTIFICATION";
                }
            }

            // B. Phone Calling (Android & Mac)
            if (cmd.startsWith("call ") || cmd.startsWith("dial ") || cmd.contains("phone call to ") || cmd.contains("make a call to ")) {
                String target = cmd.replace("phone call to ", "").replace("make a call to ", "").replaceAll("^(call|dial)\\s+(to\\s+)?", "").trim();
                String phone = target.replaceAll("[^0-9+]", "");
                if (phone.length() < 4) phone = ContactManager.getPhone(target);

                if (phone != null && !phone.isEmpty()) {
                    String label = Character.toUpperCase(target.charAt(0)) + target.substring(1);
                    VisionAgent.speak("Calling " + label + " on your phone now.");
                    AdbManager.callPhone(phone);
                    MacAutomation.callNumber(phone);
                    return "CALL_PLACED:" + phone;
                } else {
                    VisionAgent.speak("I couldn't find a phone number for " + target + ". Please provide the number or add them to contacts.");
                    return "CONTACT_NOT_FOUND";
                }
            }

            // C. Add Contact by Voice
            if (cmd.startsWith("add contact ") || cmd.startsWith("save contact ")) {
                String rest = cmd.replace("add contact ", "").replace("save contact ", "").trim();
                String[] parts = rest.split("(?:\\s+with\\s+number\\s+|\\s+number\\s+|\\s+phone\\s+|\\s+)", 2);
                if (parts.length >= 2) {
                    String cName = parts[0].trim();
                    String cPhone = parts[1].replaceAll("[^0-9+]", "").trim();
                    if (!cName.isEmpty() && !cPhone.isEmpty()) {
                        ContactManager.setContact(cName, cPhone);
                        VisionAgent.speak("Saved " + cName + " with number " + cPhone + ".");
                        return "CONTACT_SAVED:" + cName;
                    }
                }
            }

            // D. Direct WhatsApp Messaging
            boolean isExplicitMessage = cmd.startsWith("send message to ") || cmd.startsWith("send a message to ") ||
                                        (cmd.startsWith("whatsapp ") && !cmd.equals("whatsapp") && !cmd.startsWith("whatsapp app")) ||
                                        cmd.startsWith("message ") || cmd.startsWith("text ");
            if (isExplicitMessage) {
                Pattern p1 = Pattern.compile("^(?:send (?:a )?message to|whatsapp|message|text)\\s+([a-zA-Z0-9]+)(?:\\s+(?:saying\\s+)?(.*))?", Pattern.CASE_INSENSITIVE);
                Matcher m1 = p1.matcher(rawCmd.trim());
                if (m1.find()) {
                    String cName = m1.group(1).trim();
                    String msg = m1.group(2) != null ? m1.group(2).trim() : "Hello from Vision AI!";
                    String phone = ContactManager.getPhone(cName);
                    if (phone != null) {
                        VisionAgent.speak("Sending WhatsApp message to " + cName + ": " + msg);
                        NotificationManager.sendWhatsAppMessage(phone, msg);
                        return "MESSAGE_SENT";
                    }
                }
            }

            // E. Application Launching (Mac & Android - Siri Style)
            String appQuery = null;
            if (cmd.startsWith("open ") || cmd.startsWith("launch ") || cmd.startsWith("start ") || 
                cmd.startsWith("run ") || cmd.startsWith("bring up ") || cmd.startsWith("switch to ") ||
                cmd.startsWith("open up ") || cmd.startsWith("please open ")) {
                appQuery = cmd.replaceAll("^(open|launch|start|run|bring up|switch to|open up|please open)\\s+(the\\s+)?", "").trim();
            } else {
                AppManager.AppResolution check = AppManager.resolve(cmd);
                if (check != null && (check.appEntry != null || check.webUrl != null)) appQuery = cmd;
            }

            if (appQuery != null && !appQuery.isEmpty()) {
                if (appQuery.startsWith("phone ") || appQuery.startsWith("mobile ") || 
                    appQuery.endsWith("on my phone") || appQuery.endsWith("on phone")) {
                    String phoneApp = appQuery.replace("phone ", "").replace("mobile ", "").replace(" on my phone", "").replace(" on phone", "").trim();
                    AdbManager.launchPhoneApp(phoneApp);
                    VisionAgent.speak("Opening " + phoneApp + " on your mobile device.");
                    return "OPENED_PHONE_APP:" + phoneApp;
                }

                AppManager.AppResolution res = AppManager.resolve(appQuery);
                if (res != null) {
                    if (res.appEntry != null) {
                        VisionAgent.speak("Opening " + res.appEntry.displayName + ".");
                        AppManager.launchEntry(res.appEntry);
                        return "OPENED_APP:" + res.appEntry.displayName;
                    } else if (res.webUrl != null) {
                        VisionAgent.speak("Opening " + appQuery + " in your browser.");
                        AppManager.openApp(appQuery);
                        return "OPENED_WEB:" + appQuery;
                    }
                }
                VisionAgent.speak("Searching Google for " + appQuery + ".");
                MacAutomation.searchGoogle(appQuery);
                return "SEARCHED_WEB:" + appQuery;
            }

            // F. Application Closing
            if (cmd.startsWith("close ") || cmd.startsWith("quit ") || cmd.startsWith("exit ") || cmd.startsWith("kill ")) {
                String appName = cmd.replaceAll("^(close|quit|exit|kill)\\s+", "").trim();
                AppManager.AppResolution res = AppManager.resolve(appName);
                String displayName = (res != null && res.appEntry != null) ? res.appEntry.displayName : appName;
                VisionAgent.speak("Closing " + displayName + ".");
                AppManager.closeApp(appName);
                return "CLOSED_APP:" + displayName;
            }

            // G. Volume Controls
            if (cmd.equals("mute")) { MacAutomation.mute(); VisionAgent.speak("Muted."); return "MUTED"; }
            if (cmd.equals("unmute")) { MacAutomation.unmute(); VisionAgent.speak("Unmuted."); return "UNMUTED"; }
            if (cmd.contains("volume up") || cmd.contains("louder")) { MacAutomation.volumeUp(); VisionAgent.speak("Volume increased to " + MacAutomation.getVolume() + " percent."); return "VOLUME_UP"; }
            if (cmd.contains("volume down") || cmd.contains("softer")) { MacAutomation.volumeDown(); VisionAgent.speak("Volume decreased to " + MacAutomation.getVolume() + " percent."); return "VOLUME_DOWN"; }
            if (cmd.contains("set volume to") || cmd.startsWith("volume ")) {
                Matcher m = Pattern.compile("(\\d+)").matcher(cmd);
                if (m.find()) { int lvl = Integer.parseInt(m.group(1)); MacAutomation.setVolume(lvl); VisionAgent.speak("Volume " + lvl + " percent."); return "VOLUME_SET"; }
            }

            // H. Media Controls
            if (cmd.equals("play") || cmd.equals("pause") || cmd.contains("play music") || cmd.contains("pause music")) {
                MacAutomation.playPause(); VisionAgent.speak("Toggling media."); return "MEDIA_TOGGLE";
            }
            if (cmd.contains("next song") || cmd.contains("skip song")) { MacAutomation.nextTrack(); VisionAgent.speak("Skipping track."); return "MEDIA_NEXT"; }
            if (cmd.contains("previous song")) { MacAutomation.previousTrack(); VisionAgent.speak("Previous track."); return "MEDIA_PREV"; }

            // I. System Actions
            if (cmd.contains("lock mac") || cmd.contains("lock screen")) { VisionAgent.speak("Locking screen."); MacAutomation.lockScreen(); return "SCREEN_LOCKED"; }
            if (cmd.contains("sleep mac")) { VisionAgent.speak("Putting Mac to sleep."); MacAutomation.sleepMac(); return "MAC_SLEEP"; }
            if (cmd.contains("take screenshot") || cmd.contains("screenshot")) { VisionAgent.speak("Taking screenshot."); MacAutomation.takeScreenshot(); return "SCREENSHOT"; }
            if (cmd.contains("show desktop")) { VisionAgent.speak("Showing desktop."); MacAutomation.showDesktop(); return "DESKTOP"; }
            if (cmd.contains("empty trash")) { VisionAgent.speak("Emptying trash."); MacAutomation.emptyTrash(); return "TRASH"; }
            if (cmd.contains("check battery") || cmd.contains("battery")) { String b = MacAutomation.getBatteryStatus(); VisionAgent.speak(b); return "BATTERY:" + b; }

            // J. Greetings & Conversational
            if (cmd.contains("hello") || cmd.contains("hi vision") || cmd.contains("hey vision")) {
                VisionAgent.speak("Hello sir. Vision is online and ready.");
                return "GREETING";
            }
            if (cmd.contains("who are you") || cmd.contains("what can you do") || cmd.contains("help")) {
                VisionAgent.speak("I am Vision, your AI assistant. I can open and close any application on your Mac, make phone calls, send messages, monitor and reply to notifications, control volume, and control your Android mobile device.");
                return "HELP";
            }
            if (cmd.contains("thank you") || cmd.contains("thanks")) { VisionAgent.speak("Always at your service, sir."); return "THANKS"; }

            System.out.println("[Brain] Unrecognized command: " + rawCmd);
            return "UNKNOWN_COMMAND";
        }
    }

    // =========================================================================
    // 10. UTILITIES
    // =========================================================================
    private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private static String extractParam(String query, String paramName) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equalsIgnoreCase(paramName)) {
                try { return URLDecoder.decode(kv[1], StandardCharsets.UTF_8); } catch (Exception e) { return kv[1]; }
            }
        }
        return null;
    }

    private static String extractParam(String query, String paramName, String defaultVal) {
        String val = extractParam(query, paramName);
        return val != null ? val : defaultVal;
    }

    private static String extractJsonField(String json, String field, String defaultVal) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx == -1) return defaultVal;
        int colon = json.indexOf(":", idx);
        if (colon == -1) return defaultVal;
        int startQuote = json.indexOf("\"", colon);
        if (startQuote == -1) return defaultVal;
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return defaultVal;
        return json.substring(startQuote + 1, endQuote);
    }

    private static String quoteJson(String s) {
        return s == null ? "null" : "\"" + escapeJson(s) + "\"";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}

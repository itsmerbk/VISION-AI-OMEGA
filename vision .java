import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.awt.Desktop;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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
 * VISION AI 3.0 (OMEGA EDITION) — UNIVERSAL MAC & VIVO MOBILE COMMANDER
 * ==============================================================================
 * Say: "Vision Safari", "Vision IntelliJ", "Open Calculator", "Open Facebook",
 *      "Close Safari", "Open all apps", or click any app in the "ALL APPS" drawer.
 * ==============================================================================
 */
class VisionAgent {
    private static int PORT = 8080;
    private static volatile String lastSpoken = "Vision Omega online.";
    private static String lastCommand = "";
    private static long lastCommandTime = 0;
    private static Process currentSayProcess = null;
    private static final Object speakLock = new Object();
    private static volatile boolean isSpeaking = false;

    // =========================================================================
    // 1. EMBEDDED STARK-LAB 3D HOLOGRAPHIC HUD (HTML / CSS / JS)
    // =========================================================================
    private static final String HTML_CONTENT = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>VISION AI 3.0 — UNIVERSAL MAC APP COMMANDER</title>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <style>
            :root {
              --primary: #ff2a2a;
              --primary-glow: rgba(255, 42, 42, 0.5);
              --workplace: #00f0ff;
              --workplace-glow: rgba(0, 240, 255, 0.5);
              --bg-dark: #030508;
              --panel-bg: rgba(8, 12, 20, 0.82);
              --panel-border: rgba(255, 42, 42, 0.35);
              --text-dim: #7a8b9e;
              --text-bright: #ffffff;
              --accent-yellow: #ffd000;
              --accent-green: #00ff88;
            }

            * { box-sizing: border-box; }
            body {
              margin: 0;
              overflow: hidden;
              background: radial-gradient(circle at center, #0c1220 0%, #030508 75%);
              color: var(--primary);
              font-family: 'SF Pro Display', -apple-system, 'Segoe UI', Roboto, 'Courier New', monospace;
              user-select: none;
            }

            body::before {
              content: " ";
              display: block;
              position: absolute;
              top: 0; left: 0; bottom: 0; right: 0;
              background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.28) 50%),
                          linear-gradient(90deg, rgba(255, 0, 0, 0.03), rgba(0, 255, 0, 0.01), rgba(0, 0, 255, 0.03));
              z-index: 20;
              background-size: 100% 3px, 6px 100%;
              pointer-events: none;
            }

            #reticle-center {
              position: absolute;
              top: 50%; left: 50%;
              transform: translate(-50%, -50%);
              width: 520px; height: 520px;
              border-radius: 50%;
              border: 1px dashed rgba(255, 42, 42, 0.18);
              pointer-events: none;
              z-index: 5;
              animation: spinSlow 40s linear infinite;
            }
            #reticle-inner {
              position: absolute;
              top: 50%; left: 50%;
              transform: translate(-50%, -50%);
              width: 380px; height: 380px;
              border-radius: 50%;
              border: 1px solid rgba(0, 240, 255, 0.12);
              pointer-events: none;
              z-index: 5;
              animation: spinReverse 25s linear infinite;
            }
            @keyframes spinSlow { 100% { transform: translate(-50%, -50%) rotate(360deg); } }
            @keyframes spinReverse { 100% { transform: translate(-50%, -50%) rotate(-360deg); } }

            #hud-top {
              position: absolute;
              top: 22px; left: 30px; right: 30px;
              z-index: 30;
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              pointer-events: none;
            }

            .title-block { pointer-events: auto; max-width: 580px; }
            .clock-bar {
              font-size: 11px;
              letter-spacing: 3px;
              color: var(--workplace);
              margin-bottom: 4px;
              font-weight: 700;
            }
            h1 {
              margin: 0;
              font-size: 34px;
              letter-spacing: 7px;
              font-weight: 900;
              text-transform: uppercase;
              text-shadow: 0 0 22px var(--primary-glow);
              transition: color 0.4s, text-shadow 0.4s;
            }

            #status-badge {
              display: inline-flex;
              align-items: center;
              gap: 8px;
              font-size: 11px;
              color: var(--accent-green);
              letter-spacing: 2px;
              margin-top: 6px;
              text-transform: uppercase;
              background: rgba(0, 255, 136, 0.1);
              padding: 4px 10px;
              border: 1px solid rgba(0, 255, 136, 0.35);
              border-radius: 4px;
            }
            .pulse-dot {
              width: 8px; height: 8px;
              background: var(--accent-green);
              border-radius: 50%;
              animation: pulse 1.4s infinite;
            }
            @keyframes pulse {
              0%, 100% { opacity: 0.3; transform: scale(0.9); }
              50% { opacity: 1; transform: scale(1.25); }
            }

            #voice-log {
              font-size: 15px;
              color: var(--accent-yellow);
              margin-top: 10px;
              font-weight: 700;
              letter-spacing: 1px;
              text-shadow: 0 0 10px rgba(255, 208, 0, 0.4);
            }
            #ai-response {
              font-size: 14px;
              color: #00f0ff;
              margin-top: 5px;
              letter-spacing: 1px;
              font-weight: 600;
              text-shadow: 0 0 8px rgba(0, 240, 255, 0.35);
            }

            #equalizer {
              display: flex;
              align-items: flex-end;
              gap: 3px;
              height: 24px;
              margin-top: 10px;
            }
            .eq-bar {
              width: 4px;
              height: 4px;
              background: var(--primary);
              border-radius: 2px;
              transition: height 0.08s ease, background 0.3s;
              box-shadow: 0 0 6px var(--primary-glow);
            }

            .quick-chips {
              display: flex;
              flex-wrap: wrap;
              gap: 6px;
              margin-top: 10px;
              pointer-events: auto;
            }
            .chip {
              background: rgba(255, 255, 255, 0.05);
              border: 1px solid rgba(255, 255, 255, 0.18);
              color: #ccd6e0;
              font-size: 11px;
              padding: 4px 10px;
              border-radius: 14px;
              cursor: pointer;
              letter-spacing: 1px;
              transition: all 0.2s;
            }
            .chip:hover {
              background: var(--primary);
              color: #000;
              border-color: var(--primary);
              font-weight: 700;
              transform: translateY(-1px);
            }

            .telemetry-panel {
              pointer-events: auto;
              background: var(--panel-bg);
              border: 1px solid var(--panel-border);
              backdrop-filter: blur(12px);
              padding: 14px 18px;
              border-radius: 10px;
              display: flex;
              flex-direction: column;
              gap: 8px;
              font-size: 11px;
              color: var(--text-dim);
              min-width: 285px;
              box-shadow: 0 10px 35px rgba(0,0,0,0.65);
            }
            .panel-title {
              color: var(--workplace);
              font-weight: 800;
              letter-spacing: 2px;
              border-bottom: 1px solid rgba(255,255,255,0.1);
              padding-bottom: 5px;
              margin-bottom: 2px;
              display: flex;
              justify-content: space-between;
            }
            .telemetry-row {
              display: flex;
              justify-content: space-between;
              align-items: center;
            }
            .telemetry-val {
              color: var(--text-bright);
              font-weight: 700;
              letter-spacing: 1px;
            }

            #command-history {
              margin-top: 6px;
              border-top: 1px solid rgba(255,255,255,0.1);
              padding-top: 6px;
              max-height: 95px;
              overflow-y: auto;
              font-size: 11px;
              display: flex;
              flex-direction: column;
              gap: 4px;
            }
            .hist-item {
              color: #aab8c6;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
            .hist-item b { color: var(--accent-green); }

            #notification-banner {
              position: absolute;
              top: 105px; left: 50%;
              transform: translateX(-50%) translateY(-20px);
              z-index: 100;
              background: rgba(18, 5, 10, 0.95);
              border: 2px solid var(--primary);
              box-shadow: 0 0 35px var(--primary-glow);
              border-radius: 12px;
              padding: 18px 24px;
              width: 90%;
              max-width: 560px;
              backdrop-filter: blur(15px);
              display: none;
              opacity: 0;
              transition: all 0.35s ease;
            }
            #notification-banner.active {
              display: block;
              opacity: 1;
              transform: translateX(-50%) translateY(0);
            }
            .notif-header {
              display: flex; align-items: center; justify-content: space-between;
              margin-bottom: 10px; border-bottom: 1px solid rgba(255, 42, 42, 0.25); padding-bottom: 6px;
            }
            .notif-app-tag {
              background: var(--primary); color: #000; font-size: 11px; font-weight: 800;
              padding: 3px 8px; border-radius: 4px; letter-spacing: 1.5px;
            }
            .notif-sender { font-size: 16px; font-weight: 800; color: #fff; letter-spacing: 1px; }
            .notif-body {
              font-size: 14px; color: #e0e0e0; margin-bottom: 14px;
              background: rgba(255, 255, 255, 0.05); padding: 10px 14px; border-radius: 6px;
              border-left: 3px solid var(--primary);
            }
            .quick-replies { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
            .reply-chip {
              background: rgba(255, 255, 255, 0.1); border: 1px solid rgba(255, 255, 255, 0.2);
              color: #fff; padding: 5px 12px; border-radius: 20px; font-size: 12px; cursor: pointer;
            }
            .reply-chip:hover { background: var(--primary); color: #000; }
            .notif-actions { display: flex; gap: 10px; }

            #controls {
              position: absolute;
              bottom: 24px; left: 30px; right: 30px;
              z-index: 30;
              display: flex;
              justify-content: space-between;
              align-items: center;
              flex-wrap: wrap;
              gap: 12px;
              pointer-events: none;
            }
            .btn-group {
              display: flex;
              gap: 10px;
              pointer-events: auto;
              align-items: center;
              flex-wrap: wrap;
            }

            button {
              background: rgba(255, 42, 42, 0.15);
              color: var(--primary);
              border: 1px solid var(--primary);
              padding: 11px 16px;
              cursor: pointer;
              letter-spacing: 1.5px;
              font-size: 12px;
              font-weight: 700;
              border-radius: 6px;
              backdrop-filter: blur(8px);
              transition: all 0.2s ease;
              display: inline-flex;
              align-items: center;
              gap: 6px;
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
              border-color: rgba(255,255,255,0.28);
              color: var(--text-dim);
              background: rgba(255,255,255,0.06);
            }
            button.btn-secondary:hover {
              border-color: #fff;
              color: #fff;
              background: rgba(255,255,255,0.2);
            }

            #cmd-input {
              background: rgba(10, 15, 24, 0.85);
              border: 1px solid var(--workplace);
              color: #fff;
              padding: 11px 14px;
              border-radius: 6px;
              font-size: 13px;
              width: 260px;
              outline: none;
              font-family: inherit;
              box-shadow: 0 0 12px rgba(0, 240, 255, 0.15);
            }

            .modal-overlay {
              position: fixed; top: 0; left: 0; right: 0; bottom: 0;
              background: rgba(0, 0, 0, 0.85); z-index: 200;
              display: none; align-items: center; justify-content: center;
              backdrop-filter: blur(12px);
            }
            .modal-overlay.active { display: flex; }
            .modal-card {
              background: #0b101b; border: 1px solid var(--panel-border);
              width: 90%; max-width: 720px; max-height: 82vh;
              border-radius: 12px; display: flex; flex-direction: column;
              box-shadow: 0 15px 45px rgba(0,0,0,0.85); overflow: hidden;
            }
            .modal-header {
              padding: 16px 24px; border-bottom: 1px solid rgba(255,255,255,0.1);
              display: flex; justify-content: space-between; align-items: center;
            }
            .modal-header h3 { margin: 0; color: #fff; font-size: 17px; letter-spacing: 2px; }
            .modal-body { padding: 20px 24px; overflow-y: auto; flex: 1; }
            .search-input {
              width: 100%; background: rgba(255,255,255,0.07);
              border: 1px solid rgba(255,255,255,0.2); color: #fff;
              padding: 11px 15px; border-radius: 6px; font-size: 14px;
              margin-bottom: 16px; outline: none;
            }
            .app-grid {
              display: grid; grid-template-columns: repeat(auto-fill, minmax(175px, 1fr)); gap: 10px;
            }
            .app-item {
              background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.1);
              padding: 10px 14px; border-radius: 6px; display: flex; align-items: center;
              justify-content: space-between; cursor: pointer; transition: all 0.2s;
            }
            .app-item:hover {
              background: rgba(0, 240, 255, 0.15); border-color: var(--workplace);
              transform: translateY(-1px);
            }
            .app-item span { color: #fff; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

            video { display: none; }
          </style>
          <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
          <script src="https://cdn.jsdelivr.net/npm/@mediapipe/camera_utils/camera_utils.js" crossorigin="anonymous"></script>
          <script src="https://cdn.jsdelivr.net/npm/@mediapipe/hands/hands.js" crossorigin="anonymous"></script>
        </head>
        <body>
          <div id="reticle-center"></div>
          <div id="reticle-inner"></div>

          <div id="hud-top">
            <div class="title-block">
              <div class="clock-bar" id="live-clock">STARK OS // SYSTEM TIME: --:--:--</div>
              <h1 id="title">VISION OMEGA</h1>
              <div id="status-badge"><span class="pulse-dot"></span> <span id="status-text">ALL MAC APPS INDEXED & READY</span></div>
              <div id="voice-log">SAY "VISION [APP NAME]" OR "OPEN [APP NAME]"</div>
              <div id="ai-response">READY FOR ANY MAC OR MOBILE APPLICATION</div>
              <div id="equalizer"></div>
              <div class="quick-chips">
                <span class="chip" onclick="openAppModal()">🚀 View All Installed Mac Apps</span>
                <span class="chip" onclick="triggerQuick('vision safari')">🧭 Safari</span>
                <span class="chip" onclick="triggerQuick('vision notes')">📝 Notes</span>
                <span class="chip" onclick="triggerQuick('vision calculator')">🧮 Calculator</span>
                <span class="chip" onclick="triggerQuick('vision system settings')">⚙️ Settings</span>
                <span class="chip" onclick="triggerQuick('vision terminal')">💻 Terminal</span>
                <span class="chip" onclick="triggerQuick('vision facebook')">🌐 Facebook</span>
                <span class="chip" onclick="triggerQuick('vision whatsapp')">💬 WhatsApp</span>
              </div>
            </div>

            <div class="telemetry-panel">
              <div class="panel-title"><span>SYSTEM TELEMETRY</span><span>MARK VIII</span></div>
              <div class="telemetry-row"><span>MAC BATTERY</span><span class="telemetry-val" id="mac-batt">SCANNING...</span></div>
              <div class="telemetry-row"><span>AUDIO OUTPUT</span><span class="telemetry-val" id="mac-vol">50%</span></div>
              <div class="telemetry-row"><span>CPU / MEMORY</span><span class="telemetry-val" id="mac-cpu">OPTIMAL</span></div>
              <div class="telemetry-row"><span>MAC APPS READY</span><span class="telemetry-val" id="app-count" style="color:var(--accent-green); cursor:pointer;" onclick="openAppModal()">...</span></div>
              <div class="telemetry-row"><span>VIVO PHONE (ADB)</span><span class="telemetry-val" id="phone-status">CHECKING...</span></div>
              <div class="telemetry-row"><span>HOLOGRAM MODE</span><span class="telemetry-val" id="holo-mode-label">CORE SPHERE</span></div>
              <div id="command-history">
                <div class="hist-item"><b>[SYS]</b> Universal Mac App Launcher Ready.</div>
              </div>
            </div>
          </div>

          <div id="notification-banner">
            <div class="notif-header">
              <span class="notif-app-tag" id="notif-app">WHATSAPP</span>
              <span class="notif-sender" id="notif-sender">RAHUL</span>
            </div>
            <div class="notif-body" id="notif-text">"Hey bro, are you available for the team meeting?"</div>
            <div class="quick-replies">
              <div class="reply-chip" onclick="quickReply('I will be there in 10 minutes.')">I'll be there in 10 mins</div>
              <div class="reply-chip" onclick="quickReply('Sounds good, let us do it.')">Sounds good!</div>
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
                <h3 id="modal-app-title">ALL INSTALLED MACBOOK APPLICATIONS</h3>
                <button class="btn-secondary" style="padding: 6px 12px;" onclick="closeAppModal()">✕</button>
              </div>
              <div class="modal-body">
                <input type="text" id="app-search" class="search-input" placeholder="Type any Mac app name or say 'Vision [App Name]'..." oninput="filterApps()">
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

          <div id="controls">
            <div class="btn-group">
              <button id="mic-btn" onclick="toggleVoice()">🎙️ ACTIVATE MIC (SPACE)</button>
              <button class="btn-cyan" onclick="openAppModal()">💻 ALL MAC APPS</button>
              <button class="btn-secondary" style="border-color:#ff4444; color:#ff4444;" onclick="silenceVision()">⏹️ SILENCE</button>
              <button class="btn-secondary" style="border-color:#00ff88; color:#00ff88;" onclick="promptVoiceCall()">📞 CALL</button>
              <button class="btn-cyan" onclick="cycleHoloMode()">🔄 MORPH HOLOGRAM</button>
              <button class="btn-secondary" onclick="openContactModal()">👥 CONTACTS</button>
            </div>
            <div class="btn-group">
              <input id="cmd-input" type="text" placeholder="Say or type: vision [any app name]..." onkeydown="if(event.key==='Enter') runTypedCommand()">
              <button class="btn-cyan" onclick="runTypedCommand()">▶ LAUNCH</button>
            </div>
          </div>

          <video id="webcam" autoplay playsinline></video>

          <script>
            setInterval(() => {
              const now = new Date();
              document.getElementById('live-clock').innerText = "STARK OS // " + now.toLocaleDateString() + " // " + now.toLocaleTimeString();
            }, 1000);

            const eqContainer = document.getElementById('equalizer');
            const eqBars = [];
            for (let i = 0; i < 28; i++) {
              const b = document.createElement('div');
              b.className = 'eq-bar';
              eqContainer.appendChild(b);
              eqBars.push(b);
            }

            const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
            function playBeep(freq = 880, type = 'sine', duration = 0.14) {
              try {
                if (audioCtx.state === 'suspended') audioCtx.resume();
                const osc = audioCtx.createOscillator();
                const gain = audioCtx.createGain();
                osc.type = type;
                osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
                gain.gain.setValueAtTime(0.12, audioCtx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + duration);
                osc.connect(gain);
                gain.connect(audioCtx.destination);
                osc.start();
                osc.stop(audioCtx.currentTime + duration);
              } catch(e) {}
            }
            function playJarvisChime() {
              playBeep(523.25, 'triangle', 0.09);
              setTimeout(() => playBeep(1046.50, 'sine', 0.18), 90);
            }
            function playAlertPing() {
              playBeep(987.77, 'sawtooth', 0.12);
              setTimeout(() => playBeep(1318.51, 'sine', 0.25), 120);
            }

            // 3D Particle Engine
            const scene = new THREE.Scene();
            const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
            const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
            renderer.setSize(window.innerWidth, window.innerHeight);
            renderer.setPixelRatio(window.devicePixelRatio);
            document.body.appendChild(renderer.domElement);

            const particleCount = 9000;
            const geometry = new THREE.BufferGeometry();
            const positions = new Float32Array(particleCount * 3);
            const sphereTargets = new Float32Array(particleCount * 3);
            const armorTargets = new Float32Array(particleCount * 3);
            const helixTargets = new Float32Array(particleCount * 3);
            const galaxyTargets = new Float32Array(particleCount * 3);
            const colors = new Float32Array(particleCount * 3);

            for (let i = 0; i < particleCount; i++) {
              if (i < particleCount * 0.8) {
                const r = 2.3 * Math.cbrt(Math.random());
                const theta = Math.random() * 2 * Math.PI;
                const phi = Math.acos(2 * Math.random() - 1);
                sphereTargets[i*3] = r * Math.sin(phi) * Math.cos(theta);
                sphereTargets[i*3+1] = r * Math.sin(phi) * Math.sin(theta);
                sphereTargets[i*3+2] = r * Math.cos(phi);
              } else {
                const angle = Math.random() * Math.PI * 2;
                const ringR = 3.1 + (Math.random() - 0.5) * 0.25;
                sphereTargets[i*3] = ringR * Math.cos(angle);
                sphereTargets[i*3+1] = (Math.random() - 0.5) * 0.15;
                sphereTargets[i*3+2] = ringR * Math.sin(angle);
              }

              const section = Math.random();
              if (section < 0.2) {
                armorTargets[i*3] = (Math.random() - 0.5) * 0.85;
                armorTargets[i*3+1] = 1.8 + Math.random() * 0.85;
                armorTargets[i*3+2] = (Math.random() - 0.5) * 0.85;
              } else if (section < 0.6) {
                armorTargets[i*3] = (Math.random() - 0.5) * 1.9;
                armorTargets[i*3+1] = (Math.random() - 0.2) * 2.1;
                armorTargets[i*3+2] = (Math.random() - 0.5) * 1.0;
              } else if (section < 0.8) {
                const side = Math.random() > 0.5 ? 1 : -1;
                armorTargets[i*3] = side * (1.1 + Math.random() * 2.3);
                armorTargets[i*3+1] = 0.5 + (Math.random() - 0.5) * 1.6;
                armorTargets[i*3+2] = (Math.random() - 0.5) * 0.6;
              } else {
                const leg = Math.random() > 0.5 ? 0.6 : -0.6;
                armorTargets[i*3] = leg + (Math.random() - 0.5) * 0.6;
                armorTargets[i*3+1] = -0.5 - Math.random() * 2.2;
                armorTargets[i*3+2] = (Math.random() - 0.5) * 0.6;
              }

              const t = (i / particleCount) * Math.PI * 10 - Math.PI * 5;
              const strand = (i % 2 === 0) ? 1 : -1;
              helixTargets[i*3] = Math.cos(t) * 1.6 * strand + (Math.random() - 0.5) * 0.25;
              helixTargets[i*3+1] = t * 0.22;
              helixTargets[i*3+2] = Math.sin(t) * 1.6 * strand + (Math.random() - 0.5) * 0.25;

              const arm = i % 3;
              const rad = Math.pow(Math.random(), 0.7) * 3.6;
              const ang = rad * 2.5 + (arm * 2 * Math.PI / 3) + (Math.random() - 0.5) * 0.4;
              galaxyTargets[i*3] = Math.cos(ang) * rad;
              galaxyTargets[i*3+1] = (Math.random() - 0.5) * (0.5 - rad * 0.1);
              galaxyTargets[i*3+2] = Math.sin(ang) * rad;

              positions[i*3] = sphereTargets[i*3];
              positions[i*3+1] = sphereTargets[i*3+1];
              positions[i*3+2] = sphereTargets[i*3+2];
              colors[i*3] = 1.0; colors[i*3+1] = 0.16; colors[i*3+2] = 0.16;
            }

            geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
            geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
            const material = new THREE.PointsMaterial({
              size: 0.038, vertexColors: true, transparent: true, opacity: 0.88, blending: THREE.AdditiveBlending
            });
            const particleSystem = new THREE.Points(geometry, material);
            scene.add(particleSystem);
            camera.position.z = 6.2;

            let holoMode = 0;
            let targetScale = 1, targetRotX = 0, targetRotY = 0;
            let serverSpeaking = false;

            const modeThemes = [
              { name: "VISION OMEGA", label: "CORE SPHERE", hex: "#ff2a2a", glow: "rgba(255,42,42,0.5)", r: 1.0, g: 0.16, b: 0.16 },
              { name: "WORKPLACE ARMOR", label: "MARK LXXXV", hex: "#00f0ff", glow: "rgba(0,240,255,0.5)", r: 0.0, g: 0.94, b: 1.0 },
              { name: "BIOMETRIC HELIX", label: "DNA MATRIX", hex: "#00ff88", glow: "rgba(0,255,136,0.5)", r: 0.0, g: 1.0, b: 0.53 },
              { name: "QUANTUM VORTEX", label: "GALAXY CORE", hex: "#ffd000", glow: "rgba(255,208,0,0.5)", r: 1.0, g: 0.82, b: 0.0 }
            ];

            function setHoloMode(idx) {
              holoMode = idx % 4;
              const th = modeThemes[holoMode];
              const colAttr = geometry.attributes.color.array;
              for (let i = 0; i < particleCount; i++) {
                colAttr[i*3] = th.r; colAttr[i*3+1] = th.g; colAttr[i*3+2] = th.b;
              }
              geometry.attributes.color.needsUpdate = true;
              document.documentElement.style.setProperty('--primary', th.hex);
              document.documentElement.style.setProperty('--primary-glow', th.glow);
              document.getElementById('title').innerText = th.name;
              document.getElementById('holo-mode-label').innerText = th.label;
              playJarvisChime();
            }
            function cycleHoloMode() { setHoloMode(holoMode + 1); }

            const videoElement = document.getElementById('webcam');
            const statusText = document.getElementById('status-text');
            try {
              const hands = new Hands({ locateFile: (file) => `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}` });
              hands.setOptions({ maxNumHands: 2, modelComplexity: 1, minDetectionConfidence: 0.6, minTrackingConfidence: 0.6 });
              hands.onResults((results) => {
                if (results.multiHandLandmarks && results.multiHandLandmarks.length > 0) {
                  statusText.innerText = `GESTURE LOCKED (${results.multiHandLandmarks.length} HAND)`;
                  const h1 = results.multiHandLandmarks[0];
                  targetRotY = (h1[9].x - 0.5) * 5.5;
                  targetRotX = (h1[9].y - 0.5) * 5.5;
                  if (results.multiHandLandmarks.length === 2) {
                    const h2 = results.multiHandLandmarks[1];
                    targetScale = Math.max(0.4, Math.hypot(h1[9].x - h2[9].x, h1[9].y - h2[9].y) * 3.5);
                  } else {
                    targetScale = Math.max(0.4, Math.hypot(h1[4].x - h1[8].x, h1[4].y - h1[8].y) * 6);
                  }
                } else {
                  statusText.innerText = "ALL MAC APPS INDEXED & READY";
                  targetRotY += 0.006;
                }
              });
              const cam = new Camera(videoElement, { onFrame: async () => { await hands.send({ image: videoElement }); }, width: 640, height: 480 });
              cam.start();
            } catch(e) {}

            function animate() {
              requestAnimationFrame(animate);
              const posAttr = geometry.attributes.position.array;
              const targetArr = holoMode === 1 ? armorTargets : (holoMode === 2 ? helixTargets : (holoMode === 3 ? galaxyTargets : sphereTargets));
              for (let i = 0; i < particleCount * 3; i++) {
                posAttr[i] += (targetArr[i] - posAttr[i]) * 0.065;
              }
              geometry.attributes.position.needsUpdate = true;
              particleSystem.rotation.y += (targetRotY - particleSystem.rotation.y) * 0.1;
              particleSystem.rotation.x += (targetRotX - particleSystem.rotation.x) * 0.1;

              let pulseBoost = serverSpeaking ? (1 + Math.sin(Date.now() * 0.02) * 0.12) : 1;
              const s = particleSystem.scale.x + (targetScale * pulseBoost - particleSystem.scale.x) * 0.12;
              particleSystem.scale.set(s, s, s);

              for (let i = 0; i < eqBars.length; i++) {
                const active = serverSpeaking || userMicActive;
                const h = active ? (4 + Math.abs(Math.sin(Date.now() * 0.015 + i * 0.5)) * (serverSpeaking ? 20 : 9)) : 4;
                eqBars[i].style.height = h + 'px';
              }
              renderer.render(scene, camera);
            }
            animate();

            window.addEventListener('resize', () => {
              camera.aspect = window.innerWidth / window.innerHeight;
              camera.updateProjectionMatrix();
              renderer.setSize(window.innerWidth, window.innerHeight);
            });

            // Voice Engine
            const voiceLog = document.getElementById('voice-log');
            const aiResponse = document.getElementById('ai-response');
            const micBtn = document.getElementById('mic-btn');
            const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
            let recognition = null;
            let userMicActive = false;
            let isProcessing = false;
            let lastSentText = "";
            let lastSentTime = 0;

            function logHistory(cmd, reply) {
              const box = document.getElementById('command-history');
              const row = document.createElement('div');
              row.className = 'hist-item';
              row.innerHTML = `<b>&gt; ${cmd}:</b> ${reply}`;
              box.prepend(row);
            }

            function initVoice() {
              if (!SpeechRecognition) {
                voiceLog.innerText = "Use Google Chrome for voice, or type any app name in the bottom-right box!";
                return;
              }
              recognition = new SpeechRecognition();
              recognition.continuous = true;
              recognition.interimResults = true;
              recognition.lang = 'en-IN';

              recognition.onstart = () => {
                userMicActive = true;
                micBtn.innerHTML = "🔴 LISTENING (SPACE)";
                micBtn.style.borderColor = "var(--accent-green)";
                micBtn.style.color = "var(--accent-green)";
                voiceLog.innerText = "🎙️ SAY: 'Vision [App Name]' or 'Open [App Name]'...";
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
                let phrase = final.trim();
                if (phrase.length >= 2) {
                  handleVoiceCommand(phrase);
                }
              };

              recognition.onend = () => {
                if (userMicActive && !isProcessing) {
                  setTimeout(() => { if (userMicActive && !isProcessing) try { recognition.start(); } catch(e){} }, 250);
                } else if (!isProcessing) {
                  micBtn.innerHTML = "🎙️ ACTIVATE MIC (SPACE)";
                  micBtn.style.borderColor = "var(--primary)";
                  micBtn.style.color = "var(--primary)";
                }
              };

              recognition.onerror = () => {
                if (userMicActive && !isProcessing) {
                  setTimeout(() => { if (userMicActive && !isProcessing) try { recognition.start(); } catch(e){} }, 350);
                }
              };
            }

            function triggerQuick(cmd) { handleVoiceCommand(cmd); }

            function runTypedCommand() {
              const input = document.getElementById('cmd-input');
              const val = input.value.trim();
              if (!val) return;
              input.value = '';
              handleVoiceCommand(val);
            }

            function handleVoiceCommand(text) {
              const clean = text.replace(/[.?!,]+$/g, '').trim();
              const now = Date.now();
              if (clean.toLowerCase() === lastSentText.toLowerCase() && (now - lastSentTime < 2200)) return;
              lastSentText = clean;
              lastSentTime = now;

              const lower = clean.toLowerCase();
              if (lower.includes("show all apps") || lower.includes("list all apps") || lower.includes("all applications")) {
                openAppModal();
              }

              isProcessing = true;
              voiceLog.innerText = `YOU: "${clean}"`;
              micBtn.innerHTML = "⏳ LAUNCHING...";
              playBeep(880, 'sine', 0.08);

              try { if (recognition) recognition.abort(); } catch(e) {}

              if (lower.includes("workplace") || lower.includes("workspace") || lower.includes("armor mode")) setHoloMode(1);
              else if (lower.includes("helix mode") || lower.includes("dna mode")) setHoloMode(2);
              else if (lower.includes("galaxy mode") || lower.includes("vortex mode")) setHoloMode(3);
              else if (lower.includes("core mode") || lower.includes("sphere mode")) setHoloMode(0);

              sendVoiceCommand(clean);
            }

            function toggleVoice() {
              if (!recognition) initVoice();
              if (!recognition) return;
              if (userMicActive) {
                userMicActive = false; isProcessing = false;
                try { recognition.abort(); } catch(e) {}
                micBtn.innerHTML = "🎙️ ACTIVATE MIC (SPACE)";
                micBtn.style.borderColor = "var(--primary)";
                micBtn.style.color = "var(--primary)";
                voiceLog.innerText = "MIC PAUSED";
              } else {
                userMicActive = true; isProcessing = false;
                try { recognition.start(); } catch(e) {}
              }
            }

            function silenceVision() {
              fetch('/api/stop', { method: 'POST' });
              aiResponse.innerText = "VISION: Speech Silenced.";
              isProcessing = false;
              serverSpeaking = false;
              if (userMicActive && recognition) try { recognition.start(); } catch(e) {}
              playBeep(440, 'triangle', 0.1);
            }

            function promptVoiceCall() {
              const target = prompt("Enter contact name (Rahul, Mom, Dad, Boss) or phone number to call:");
              if (target && target.trim()) handleVoiceCommand("call " + target.trim());
            }

            function sendVoiceCommand(text) {
              aiResponse.innerText = "LAUNCHING: " + text + "...";
              serverSpeaking = true;
              fetch('/command?text=' + encodeURIComponent(text))
                .then(r => r.json())
                .then(data => {
                  const spoken = data.spoken || "Command executed.";
                  aiResponse.innerText = "VISION: " + spoken;
                  logHistory(text, data.action || "OK");
                  fetchStatus();
                  setTimeout(() => {
                    isProcessing = false;
                    serverSpeaking = false;
                    if (userMicActive && recognition) {
                      try { recognition.start(); } catch(e) {}
                      micBtn.innerHTML = "🔴 LISTENING (SPACE)";
                      micBtn.style.borderColor = "var(--accent-green)";
                      micBtn.style.color = "var(--accent-green)";
                    } else {
                      micBtn.innerHTML = "🎙️ ACTIVATE MIC (SPACE)";
                      micBtn.style.borderColor = "var(--primary)";
                      micBtn.style.color = "var(--primary)";
                    }
                  }, 1800);
                })
                .catch(() => {
                  isProcessing = false;
                  serverSpeaking = false;
                  aiResponse.innerText = "ERROR: Backend unreachable.";
                });
            }

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
            function quickReply(text) { handleVoiceCommand("reply saying " + text); }
            function promptVoiceReply() { if (!userMicActive) toggleVoice(); voiceLog.innerText = "🎙️ SPEAK YOUR REPLY NOW..."; }
            function dismissNotif() {
              handleVoiceCommand("dismiss");
              document.getElementById('notification-banner').classList.remove('active');
              currentPendingId = null;
            }

            function fetchStatus() {
              fetch('/api/status').then(r => r.json()).then(data => {
                if (data.battery) document.getElementById('mac-batt').innerText = data.battery;
                if (data.volume !== undefined) document.getElementById('mac-vol').innerText = data.volume + '%';
                if (data.cpu) document.getElementById('mac-cpu').innerText = data.cpu;
                if (data.appCount !== undefined) document.getElementById('app-count').innerText = data.appCount + ' APPS (CLICK)';
                if (data.phoneStatus) document.getElementById('phone-status').innerText = data.phoneStatus;
                serverSpeaking = Boolean(data.isSpeaking);
                checkNotifications(data);
              }).catch(() => {});
            }
            setInterval(fetchStatus, 2000);
            fetchStatus();

            // App Directory Modal (Shows ALL Installed Mac Apps)
            let allInstalledApps = [];
            function openAppModal() {
              document.getElementById('app-modal').classList.add('active');
              fetch('/api/apps').then(r => r.json()).then(apps => {
                allInstalledApps = apps;
                document.getElementById('modal-app-title').innerText = `ALL INSTALLED MACBOOK APPLICATIONS (${apps.length})`;
                renderAppGrid(apps);
              });
            }
            function closeAppModal() { document.getElementById('app-modal').classList.remove('active'); }
            function renderAppGrid(list) {
              const container = document.getElementById('app-grid-list');
              container.innerHTML = '';
              list.forEach(app => {
                const item = document.createElement('div');
                item.className = 'app-item';
                item.innerHTML = `<span>${app.displayName}</span><span style="font-size:10px; color:var(--workplace);">OPEN</span>`;
                item.onclick = () => { handleVoiceCommand("open " + app.displayName); closeAppModal(); };
                container.appendChild(item);
              });
            }
            function filterApps() {
              const q = document.getElementById('app-search').value.toLowerCase();
              renderAppGrid(allInstalledApps.filter(a => a.displayName.toLowerCase().includes(q)));
            }

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
                  row.onclick = () => { handleVoiceCommand("call " + name); closeContactModal(); };
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
        System.out.println("[VISION OMEGA]: " + text);

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
                    currentSayProcess = new ProcessBuilder("/usr/bin/say", text).start();
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
        System.out.println("   VISION AI 3.0 OMEGA - UNIVERSAL APP COMMANDER  ");
        System.out.println("==================================================");

        ContactManager.load();
        AppManager.initialize();
        AdbManager.detectAdbPath();

        for (int p = 8080; p <= 8090; p++) {
            try (ServerSocket ss = new ServerSocket(p)) {
                PORT = p;
                break;
            } catch (IOException ignored) {}
        }

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
                boolean isDuplicate = (cmd != null) && !lastCommand.isEmpty() && (
                    cmd.equalsIgnoreCase(lastCommand) && (now - lastCommandTime < 2200)
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
            json.append("\"cpu\":").append(quoteJson(MacAutomation.getCpuLoad())).append(",");
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

        String url = "http://localhost:" + PORT;
        System.out.println("[VISION OMEGA]: Server running at " + url);
        try { new ProcessBuilder("/usr/bin/open", url).start(); } catch (Exception ignored) {}
        speak("Vision online. Say Vision followed by any application name on your MacBook to launch it.");
    }

    // =========================================================================
    // 4. UNIVERSAL MAC APPLICATION MANAGER (DEEP RECURSIVE SCAN + FUZZY MATCH)
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
            aliasMap.put("visual studio", "Visual Studio Code");
            aliasMap.put("intellij", "IntelliJ IDEA");
            aliasMap.put("idea", "IntelliJ IDEA");
            aliasMap.put("intellij idea", "IntelliJ IDEA");
            aliasMap.put("calc", "Calculator");
            aliasMap.put("calculator", "Calculator");
            aliasMap.put("settings", "System Settings");
            aliasMap.put("system settings", "System Settings");
            aliasMap.put("system preferences", "System Settings");
            aliasMap.put("preferences", "System Settings");
            aliasMap.put("terminal", "Terminal");
            aliasMap.put("term", "Terminal");
            aliasMap.put("iterm", "iTerm");
            aliasMap.put("whatsapp", "WhatsApp");
            aliasMap.put("wa", "WhatsApp");
            aliasMap.put("music", "Music");
            aliasMap.put("apple music", "Music");
            aliasMap.put("spotify", "Spotify");
            aliasMap.put("notes", "Notes");
            aliasMap.put("apple notes", "Notes");
            aliasMap.put("messages", "Messages");
            aliasMap.put("imessage", "Messages");
            aliasMap.put("mail", "Mail");
            aliasMap.put("photos", "Photos");
            aliasMap.put("safari", "Safari");
            aliasMap.put("finder", "Finder");
            aliasMap.put("preview", "Preview");
            aliasMap.put("calendar", "Calendar");
            aliasMap.put("facetime", "FaceTime");
            aliasMap.put("app store", "App Store");
            aliasMap.put("store", "App Store");
            aliasMap.put("activity monitor", "Activity Monitor");
            aliasMap.put("task manager", "Activity Monitor");
            aliasMap.put("keynote", "Keynote");
            aliasMap.put("pages", "Pages");
            aliasMap.put("numbers", "Numbers");
            aliasMap.put("word", "Microsoft Word");
            aliasMap.put("excel", "Microsoft Excel");
            aliasMap.put("powerpoint", "Microsoft PowerPoint");
            aliasMap.put("zoom", "zoom.us");
            aliasMap.put("discord", "Discord");
            aliasMap.put("slack", "Slack");
            aliasMap.put("telegram", "Telegram");
            aliasMap.put("vlc", "VLC");
            aliasMap.put("postman", "Postman");
            aliasMap.put("docker", "Docker");
            aliasMap.put("xcode", "Xcode");
            aliasMap.put(" voice memos", "Voice Memos");
            aliasMap.put("reminders", "Reminders");
            aliasMap.put("clock", "Clock");
            aliasMap.put("weather", "Weather");
            aliasMap.put("maps", "Maps");
            aliasMap.put("books", "Books");
            aliasMap.put("podcasts", "Podcasts");
            aliasMap.put("tv", "TV");
            aliasMap.put("freeform", "Freeform");
            aliasMap.put("shortcuts", "Shortcuts");
            aliasMap.put("quicktime", "QuickTime Player");
            aliasMap.put("quicktime player", "QuickTime Player");
            aliasMap.put("photo booth", "Photo Booth");
            aliasMap.put("font book", "Font Book");
            aliasMap.put("disk utility", "Disk Utility");
            aliasMap.put("chess", "Chess");
            aliasMap.put("dictionary", "Dictionary");
            aliasMap.put("passwords", "Passwords");
            aliasMap.put("iphone mirroring", "iPhone Mirroring");

            webServices.put("facebook", "https://www.facebook.com");
            webServices.put("fb", "https://www.facebook.com");
            webServices.put("whatsapp", "https://web.whatsapp.com");
            webServices.put("youtube", "https://www.youtube.com");
            webServices.put("unstop", "https://unstop.com");
            webServices.put("netflix", "https://www.netflix.com");
            webServices.put("github", "https://www.github.com");
            webServices.put("reddit", "https://www.reddit.com");
            webServices.put("twitter", "https://twitter.com");
            webServices.put("x", "https://x.com");
            webServices.put("instagram", "https://www.instagram.com");
            webServices.put("linkedin", "https://www.linkedin.com");
            webServices.put("chatgpt", "https://chatgpt.com");
            webServices.put("gmail", "https://mail.google.com");
            webServices.put("google", "https://www.google.com");
            webServices.put("leetcode", "https://leetcode.com");
            webServices.put("hackerrank", "https://www.hackerrank.com");
            webServices.put("canva", "https://www.canva.com");
            webServices.put("figma", "https://www.figma.com");
            webServices.put("amazon", "https://www.amazon.in");
            webServices.put("flipkart", "https://www.flipkart.com");
            webServices.put("play store", "https://play.google.com");
        }

        public static void initialize() {
            scanDirectoriesSync();
            new Thread(AppManager::scanSpotlightAsync, "AppScanner-Thread").start();
        }

        public static void scanDirectoriesSync() {
            String home = System.getProperty("user.home");
            String[] appDirs = {
                "/Applications",
                "/Applications/Utilities",
                "/System/Applications",
                "/System/Applications/Utilities",
                "/System/Library/CoreServices/Applications",
                home + "/Applications",
                "/System/Library/CoreServices/Finder.app"
            };
            for (String dirPath : appDirs) {
                scanFolderRecursive(new File(dirPath), 0);
            }
            System.out.println("[AppManager] Instant-indexed " + indexedApps.size() + " Mac applications.");
        }

        private static void scanFolderRecursive(File file, int depth) {
            if (file == null || !file.exists() || depth > 2) return;
            if (file.getName().endsWith(".app")) {
                addAppPath(file.getAbsolutePath(), indexedApps);
                return; // Do not recurse inside a .app bundle
            }
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                if (children != null) {
                    for (File child : children) {
                        scanFolderRecursive(child, depth + 1);
                    }
                }
            }
        }

        public static void scanSpotlightAsync() {
            try {
                Process p = new ProcessBuilder("mdfind", "kMDItemContentType == 'com.apple.application-bundle'").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) addAppPath(line.trim(), indexedApps);
                }
                p.waitFor(3, TimeUnit.SECONDS);
                System.out.println("[AppManager] Spotlight deep scan complete: " + indexedApps.size() + " total Mac apps ready.");
            } catch (Exception ignored) {}
        }

        private static void addAppPath(String path, Map<String, AppEntry> map) {
            if (!path.endsWith(".app")) return;
            if (path.contains("/Frameworks/") || path.contains("/PrivateFrameworks/") ||
                path.contains("/Input Methods/") || path.contains("/Contents/") ||
                (path.contains("/Library/CoreServices/") && !path.contains("Finder.app") && !path.contains("/CoreServices/Applications/"))) {
                return;
            }
            File file = new File(path);
            String cleanName = file.getName().substring(0, file.getName().length() - 4);
            map.put(cleanName.toLowerCase(), new AppEntry(cleanName, path));
        }

        public static String normalize(String s) {
            return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9]", "");
        }

        public static String cleanSpokenQuery(String query) {
            if (query == null) return "";
            return query.toLowerCase()
                    .replaceAll("[^a-z0-9\\s.]", " ")
                    .replaceAll("\\b(hey|hi|hello|vision|jarvis|please|can|you|could|would|open|launch|start|run|bring|up|switch|to|the|app|application|my|on|mac|macbook|in|now|for|me)\\b", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
        }

        public static AppResolution resolve(String rawQuery) {
            String raw = cleanSpokenQuery(rawQuery);
            String norm = normalize(raw);
            if (norm.isEmpty()) return null;

            // Stage 1: Check Alias Map (e.g. "vs code" -> "Visual Studio Code", "intellij" -> "IntelliJ IDEA")
            if (aliasMap.containsKey(raw)) {
                String target = aliasMap.get(raw);
                for (AppEntry e : indexedApps.values()) {
                    if (e.displayName.equalsIgnoreCase(target) || e.normalizedName.equals(normalize(target))) {
                        return new AppResolution(e, null);
                    }
                }
                // Even if not in indexedApps map, macOS `open -a` can open system aliases directly
                return new AppResolution(new AppEntry(target, target), null);
            }

            // Stage 2: Exact match on lower-case display name or normalized alphanumeric name
            if (indexedApps.containsKey(raw)) return new AppResolution(indexedApps.get(raw), null);
            for (AppEntry e : indexedApps.values()) {
                if (e.normalizedName.equals(norm)) return new AppResolution(e, null);
            }

            // Stage 3: StartsWith or Contains match on installed Mac apps
            for (AppEntry e : indexedApps.values()) {
                if (e.normalizedName.startsWith(norm) || norm.startsWith(e.normalizedName)) {
                    return new AppResolution(e, null);
                }
            }
            for (AppEntry e : indexedApps.values()) {
                if (e.normalizedName.contains(norm) || norm.contains(e.normalizedName)) {
                    return new AppResolution(e, null);
                }
            }

            // Stage 4: Check Known Web Services (Facebook, WhatsApp, Unstop, LinkedIn, YouTube, etc.)
            if (webServices.containsKey(raw)) return new AppResolution(null, webServices.get(raw));
            for (Map.Entry<String, String> w : webServices.entrySet()) {
                if (raw.contains(w.getKey())) return new AppResolution(null, w.getValue());
            }

            // Stage 5: Try native macOS `open -Ra "<raw>"` to verify if macOS LaunchServices knows this app
            try {
                Process p = new ProcessBuilder("/usr/bin/open", "-Ra", raw).start();
                if (p.waitFor(500, TimeUnit.MILLISECONDS) && p.exitValue() == 0) {
                    return new AppResolution(new AppEntry(raw, raw), null);
                }
            } catch (Exception ignored) {}

            // Stage 6: Universal Website Fallback
            return new AppResolution(null, "https://www." + norm + ".com");
        }

        public static boolean launchEntry(AppEntry entry) {
            if (entry == null) return false;
            System.out.println("[AppManager] Launching Mac App: " + entry.displayName + " [" + entry.path + "]");
            try {
                if (entry.path.endsWith(".app") && new File(entry.path).exists()) {
                    new ProcessBuilder("/usr/bin/open", entry.path).start();
                } else {
                    new ProcessBuilder("/usr/bin/open", "-a", entry.displayName).start();
                }
                return true;
            } catch (Exception e) {
                try {
                    new ProcessBuilder("/usr/bin/open", "-a", entry.displayName).start();
                    return true;
                } catch (Exception ignored) { return false; }
            }
        }

        public static boolean openUrl(String url) {
            System.out.println("[AppManager] Opening URL: " + url);
            try {
                new ProcessBuilder("/usr/bin/open", url).start();
                return true;
            } catch (Exception e) {
                try { Desktop.getDesktop().browse(new URI(url)); return true; } catch (Exception ignored) { return false; }
            }
        }

        public static boolean openApp(String appQuery) {
            if (appQuery == null || appQuery.trim().isEmpty()) return false;
            AppResolution res = resolve(appQuery);
            if (res == null) return false;
            if (res.appEntry != null) return launchEntry(res.appEntry);
            if (res.webUrl != null) return openUrl(res.webUrl);
            return false;
        }

        public static boolean closeApp(String appQuery) {
            if (appQuery == null || appQuery.trim().isEmpty()) return false;
            AppResolution res = resolve(appQuery);
            String name = (res != null && res.appEntry != null) ? res.appEntry.displayName : cleanSpokenQuery(appQuery);
            try {
                new ProcessBuilder("/usr/bin/osascript", "-e", "tell application \"" + name.replace("\"", "\\\"") + "\" to quit").start();
            } catch (Exception ignored) {}
            return true;
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
            defaultAppPackages.put("facebook", "com.facebook.katana");
            defaultAppPackages.put("linkedin", "com.linkedin.android");
            defaultAppPackages.put("unstop", "com.dare2compete.app");
            defaultAppPackages.put("spotify", "com.spotify.music");
            defaultAppPackages.put("camera", "com.android.camera");
            detectAdbPath();
        }

        public static void detectAdbPath() {
            String home = System.getProperty("user.home");
            String[] possiblePaths = {"adb", "/opt/homebrew/bin/adb", "/usr/local/bin/adb", home + "/Library/Android/sdk/platform-tools/adb", home + "/Downloads/platform-tools/adb"};
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
                        if (line.endsWith("device") && !line.startsWith("List")) devices.add(line.split("\\s+")[0]);
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

        public static void setBluetooth(boolean enable) {
            runAdbOnAllDevices("shell svc bluetooth " + (enable ? "enable" : "disable"));
            runAdbOnAllDevices("shell cmd bluetooth_manager " + (enable ? "enable" : "disable"));
        }

        public static void setWifi(boolean enable) {
            runAdbOnAllDevices("shell svc wifi " + (enable ? "enable" : "disable"));
        }

        public static void unlockPhone() {
            runAdbOnAllDevices("shell input keyevent 224");
            try { Thread.sleep(500); } catch (Exception ignored) {}
            runAdbOnAllDevices("shell input swipe 500 1500 500 200 300");
        }

        public static boolean callPhone(String number) {
            if (number == null || number.trim().isEmpty()) return false;
            String clean = number.replaceAll("[^0-9+]", "").trim();
            if (clean.isEmpty()) return false;
            runAdbOnAllDevices("shell input keyevent 224");
            runAdbOnAllDevices("shell am start -a android.intent.action.CALL -d tel:" + clean);
            return true;
        }

        public static boolean launchPhoneApp(String appName) {
            String clean = appName.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
            runAdbOnAllDevices("shell input keyevent 224");
            String pkg = defaultAppPackages.get(clean);
            if (pkg != null) {
                runAdbOnAllDevices("shell monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1");
                return true;
            }
            String enc = URLEncoder.encode(clean, StandardCharsets.UTF_8);
            runAdbOnAllDevices("shell am start -a android.intent.action.VIEW -d \"market://search?q=" + enc + "\"");
            return true;
        }
    }

    // =========================================================================
    // 6. MAC AUTOMATION
    // =========================================================================
    public static class MacAutomation {
        public static void runBash(String cmd) {
            try { new ProcessBuilder("/bin/zsh", "-l", "-c", cmd).start(); } catch (Exception ignored) {}
        }

        public static int getVolume() {
            try {
                Process p = new ProcessBuilder("/usr/bin/osascript", "-e", "output volume of (get volume settings)").start();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    return Integer.parseInt(r.readLine().trim());
                }
            } catch (Exception e) { return 50; }
        }

        public static void setVolume(int level) {
            runBash("osascript -e 'set volume output volume " + Math.max(0, Math.min(100, level)) + "'");
        }

        public static void volumeUp() { setVolume(getVolume() + 15); }
        public static void volumeDown() { setVolume(getVolume() - 15); }
        public static void mute() { runBash("osascript -e 'set volume output muted true'"); }
        public static void unmute() { runBash("osascript -e 'set volume output muted false'"); }

        public static void lockScreen() { runBash("pmset displaysleepnow"); }
        public static void sleepMac() { runBash("pmset sleepnow"); }
        public static void shutdownMac() { runBash("osascript -e 'tell application \"System Events\" to shut down'"); }

        public static String takeScreenshot() {
            String home = System.getProperty("user.home");
            String path = home + "/Desktop/Vision_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png";
            runBash("screencapture -x \"" + path + "\"");
            return path;
        }

        public static String getCpuLoad() {
            try {
                Process p = new ProcessBuilder("sysctl", "-n", "vm.loadavg").start();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s = r.readLine();
                    if (s != null) return "LOAD " + s.replace("{", "").replace("}", "").trim().split(" ")[0];
                }
            } catch (Exception ignored) {}
            return "NOMINAL";
        }

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
                            boolean chg = line.contains("charging") && !line.contains("discharging");
                            return words[words.length - 1] + "%" + (chg ? " (CHARGING)" : "");
                        }
                    }
                }
            } catch (Exception ignored) {}
            return "100%";
        }

        public static void searchGoogle(String query) {
            AppManager.openUrl("https://www.google.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        }

        public static void searchYouTube(String query) {
            AppManager.openUrl("https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        }

        public static void callNumber(String number) {
            if (number == null || number.trim().isEmpty()) return;
            String clean = number.replaceAll("[^0-9+]", "").trim();
            if (!clean.isEmpty()) runBash("open \"tel:" + clean + "\"");
        }
    }

    // =========================================================================
    // 7. NOTIFICATION MANAGER & CONTACTS
    // =========================================================================
    public static class NotificationManager {
        public static class NotificationItem {
            public final String id; public final String sender; public final String app;
            public final String text; public final String phone;
            public boolean replied = false;
            public NotificationItem(String sender, String app, String text, String phone) {
                this.id = "notif_" + System.currentTimeMillis();
                this.sender = sender != null ? sender.trim() : "Unknown";
                this.app = app != null ? app.trim() : "System";
                this.text = text != null ? text.trim() : "";
                this.phone = phone != null ? phone.trim() : "";
            }
        }

        private static volatile NotificationItem pendingNotification = null;
        public static NotificationItem getPendingNotification() { return pendingNotification; }
        public static boolean hasPendingNotification() { return pendingNotification != null && !pendingNotification.replied; }

        public static boolean replyToPending(String userReply) {
            if (pendingNotification == null) return false;
            NotificationItem target = pendingNotification;
            String phone = target.phone.isEmpty() ? ContactManager.getPhone(target.sender) : target.phone;
            VisionAgent.speak("Replying to " + target.sender + ": " + userReply);
            if (phone != null && !phone.isEmpty()) {
                sendWhatsAppMessage(phone, userReply);
            }
            target.replied = true;
            pendingNotification = null;
            return true;
        }

        public static void dismissPending() {
            pendingNotification = null;
            VisionAgent.speak("Notification dismissed.");
        }

        public static boolean sendWhatsAppMessage(String phone, String message) {
            if (phone == null || phone.trim().isEmpty()) return false;
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            String encodedText = URLEncoder.encode(message, StandardCharsets.UTF_8);
            return AppManager.openUrl("https://web.whatsapp.com/send?phone=" + cleanPhone + "&text=" + encodedText);
        }
    }

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
            if (!file.exists()) return;
            Properties props = new Properties();
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
                for (String key : props.stringPropertyNames()) contacts.put(key.toLowerCase().trim(), props.getProperty(key).trim());
            } catch (Exception ignored) {}
        }

        public static String getPhone(String name) {
            if (name == null) return null;
            String clean = name.toLowerCase().replaceAll("[^a-z0-9]", "").trim();
            if (contacts.containsKey(clean)) return contacts.get(clean);
            for (String key : contacts.keySet()) if (clean.contains(key) || key.contains(clean)) return contacts.get(key);
            return null;
        }

        public static void setContact(String name, String phone) {
            if (name != null && phone != null) {
                contacts.put(name.toLowerCase().trim(), phone.replaceAll("[^0-9+]", "").trim());
            }
        }

        public static Map<String, String> getAll() { return Collections.unmodifiableMap(new TreeMap<>(contacts)); }
    }

    // =========================================================================
    // 8. THE BRAIN (ACCENT-TOLERANT "VISION <APP>" UNIVERSAL LAUNCHER)
    // =========================================================================
    public static class Brain {
        public static String processCommand(String rawCmd) {
            if (rawCmd == null || rawCmd.trim().isEmpty()) return "NO_OP";

            // Strip trailing punctuation and wake words ("vision", "hey vision", "jarvis")
            String cmd = rawCmd.toLowerCase()
                    .replaceAll("[.?!,]+$", "")
                    .replaceAll("^(hey|hi|hello)\\s+(vision|jarvis)\\s*", "")
                    .replaceAll("^(vision|jarvis)\\s+", "")
                    .trim();

            if (cmd.isEmpty()) {
                VisionAgent.speak("Yes sir, I am listening. Tell me any app to open.");
                return "LISTENING";
            }
            System.out.println("\n[Brain] Processing input: \"" + cmd + "\"");

            // 1. Notification Reply
            if (NotificationManager.hasPendingNotification()) {
                if (cmd.equals("ignore") || cmd.equals("dismiss") || cmd.equals("cancel")) {
                    NotificationManager.dismissPending();
                    return "NOTIFICATION_DISMISSED";
                }
                if (cmd.startsWith("reply saying ")) {
                    NotificationManager.replyToPending(rawCmd.substring(13).trim());
                    return "REPLIED_TO_NOTIFICATION";
                }
            }

            // 2. Time, Date & System Diagnostics
            if (cmd.contains("what time") || cmd.equals("time") || cmd.contains("current time")) {
                String t = new SimpleDateFormat("hh:mm a").format(new Date());
                VisionAgent.speak("Sir, the current time is " + t + ".");
                return "TIME:" + t;
            }
            if (cmd.contains("system status") || cmd.contains("diagnostics")) {
                String b = MacAutomation.getBatteryStatus();
                int apps = AppManager.getAppCount();
                VisionAgent.speak("All systems nominal. Mac battery is at " + b + ", and " + apps + " MacBook applications are indexed.");
                return "DIAGNOSTICS_OK";
            }

            // 3. Vivo Phone Hardware Controls (ADB)
            if (cmd.contains("phone bluetooth on") || cmd.contains("turn on phone bluetooth") || cmd.contains("turn on bluetooth")) {
                VisionAgent.speak("Enabling Bluetooth on your Vivo mobile device.");
                AdbManager.setBluetooth(true);
                return "PHONE_BT_ON";
            }
            if (cmd.contains("phone bluetooth off") || cmd.contains("turn off phone bluetooth") || cmd.contains("turn off bluetooth")) {
                VisionAgent.speak("Disabling Bluetooth on your Vivo mobile device.");
                AdbManager.setBluetooth(false);
                return "PHONE_BT_OFF";
            }
            if (cmd.contains("unlock phone") || cmd.contains("unlock mobile")) {
                VisionAgent.speak("Unlocking your Vivo mobile screen.");
                AdbManager.unlockPhone();
                return "PHONE_UNLOCKED";
            }

            // 4. Phone Calling
            if (cmd.startsWith("call ") || cmd.startsWith("dial ")) {
                String target = cmd.replaceAll("^(call|dial)\\s+(to\\s+)?", "").trim();
                String phone = target.replaceAll("[^0-9+]", "");
                if (phone.length() < 4) phone = ContactManager.getPhone(target);
                if (phone != null && !phone.isEmpty()) {
                    VisionAgent.speak("Calling " + target + " now.");
                    AdbManager.callPhone(phone);
                    MacAutomation.callNumber(phone);
                    return "CALL_PLACED:" + phone;
                }
            }

            // 5. Close ANY App on Mac
            if (cmd.startsWith("close ") || cmd.startsWith("quit ") || cmd.startsWith("exit ")) {
                String appName = cmd.replaceFirst("^(close|quit|exit)\\s+", "").trim();
                VisionAgent.speak("Closing " + appName + ".");
                AppManager.closeApp(appName);
                return "CLOSED_APP:" + appName;
            }

            // 6. Volume & System Power Actions
            if (cmd.equals("mute")) { MacAutomation.mute(); VisionAgent.speak("Audio muted."); return "MUTED"; }
            if (cmd.equals("unmute")) { MacAutomation.unmute(); VisionAgent.speak("Audio restored."); return "UNMUTED"; }
            if (cmd.contains("volume up") || cmd.contains("louder")) { MacAutomation.volumeUp(); VisionAgent.speak("Volume increased."); return "VOLUME_UP"; }
            if (cmd.contains("volume down") || cmd.contains("softer")) { MacAutomation.volumeDown(); VisionAgent.speak("Volume decreased."); return "VOLUME_DOWN"; }
            if (cmd.contains("shutdown mac") || cmd.contains("shut down mac")) { VisionAgent.speak("Shutting down Mac."); MacAutomation.shutdownMac(); return "MAC_SHUTDOWN"; }
            if (cmd.contains("lock mac") || cmd.contains("lock screen")) { VisionAgent.speak("Locking screen."); MacAutomation.lockScreen(); return "SCREEN_LOCKED"; }
            if (cmd.contains("sleep mac")) { VisionAgent.speak("Putting Mac to sleep."); MacAutomation.sleepMac(); return "MAC_SLEEP"; }
            if (cmd.contains("screenshot")) { VisionAgent.speak("Capturing screenshot."); MacAutomation.takeScreenshot(); return "SCREENSHOT"; }
            if (cmd.contains("battery")) { String b = MacAutomation.getBatteryStatus(); VisionAgent.speak("Mac battery is at " + b); return "BATTERY:" + b; }

            // 7. Play YouTube Video / Song
            if (cmd.startsWith("play ") && !cmd.equals("play music")) {
                String song = cmd.substring(5).replace("on youtube", "").trim();
                VisionAgent.speak("Playing " + song + " on YouTube.");
                MacAutomation.searchYouTube(song);
                return "YOUTUBE:" + song;
            }

            // 8. UNIVERSAL APPLICATION LAUNCHER:
            // Works whether you say "Open Safari", "Vision Safari", or just "Safari"!
            String targetApp = cmd.replaceFirst("^(open|launch|start|run)\\s+(the\\s+)?", "").trim();
            if (targetApp.startsWith("phone ") || targetApp.startsWith("mobile ")) {
                String phoneApp = targetApp.replace("phone ", "").replace("mobile ", "").trim();
                VisionAgent.speak("Launching " + phoneApp + " on your Vivo phone.");
                AdbManager.launchPhoneApp(phoneApp);
                return "OPENED_PHONE_APP:" + phoneApp;
            }

            AppManager.AppResolution res = AppManager.resolve(targetApp);
            if (res != null) {
                if (res.appEntry != null) {
                    VisionAgent.speak("Opening " + res.appEntry.displayName + ".");
                    AppManager.launchEntry(res.appEntry);
                    return "OPENED_APP:" + res.appEntry.displayName;
                } else if (res.webUrl != null) {
                    VisionAgent.speak("Opening " + targetApp + ".");
                    AppManager.openUrl(res.webUrl);
                    return "OPENED_WEB:" + targetApp;
                }
            }

            VisionAgent.speak("Opening " + targetApp + ".");
            AppManager.openApp(targetApp);
            return "OPENED:" + targetApp;
        }
    }

    // =========================================================================
    // 9. UTILITIES
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

    private static String quoteJson(String s) {
        return s == null ? "null" : "\"" + escapeJson(s) + "\"";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
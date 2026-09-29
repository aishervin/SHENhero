package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RobotAvatarComponent(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    isThinking: Boolean = false
) {
    val htmlContent = """
        <!DOCTYPE html>
        <html lang="fa" dir="rtl">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                :root {
                    --eye-color: #00eaff;
                    --eye-glow-color: rgba(0, 234, 255, 0.7);
                }
                * { -webkit-tap-highlight-color: transparent; box-sizing: border-box; }
                body {
                    margin: 0; padding: 0; width: 100vw; height: 100vh;
                    background-color: transparent;
                    display: flex; align-items: center; justify-content: center;
                    overflow: hidden;
                }
                #robot-wrapper {
                    display: flex; justify-content: center; align-items: center; position: relative; width: 100%; height: 100%;
                }
                .robot-container { width: 140px; height: 140px; position: relative; }
                .robot-frame {
                    width: 100%; height: 100%;
                    background: linear-gradient(145deg, #1f2937, #0b0f19);
                    border-radius: 24%;
                    box-shadow: inset 0 0 15px rgba(0,0,0,0.9), 0 0 25px rgba(0,234,255,0.25);
                    display: flex; align-items: center; justify-content: center;
                    position: relative; overflow: hidden;
                    border: 2px solid rgba(0,234,255,0.5);
                }
                #faceCanvas { width: 82%; height: 78%; display: block; }
            </style>
        </head>
        <body>
            <div id="robot-wrapper">
                <div class="robot-container">
                    <div class="robot-frame">
                        <canvas id="faceCanvas" width="300" height="300"></canvas>
                    </div>
                </div>
            </div>
            <script>
            class RobotFace {
                constructor(canvas) {
                    this.canvas = canvas;
                    this.ctx = canvas.getContext("2d");
                    this.W = canvas.width;
                    this.H = canvas.height;
                    this.lookX = 0; this.lookY = 0;
                    this.targetLookX = 0; this.targetLookY = 0;
                    this.blinkProgress = 1; this.isBlinking = false;
                    this.lastBlinkTime = Date.now();
                    this.isSleeping = false;
                    this.lastInteractionTime = Date.now();
                    this.sleepTimeout = 20000;
                    this.isTyping = false;
                    this.isThinking = ${isThinking};
                    this.readingOscillation = 0;
                    this.thinkingStartTime = Date.now();
                    this.leftEyeScale = 1; this.rightEyeScale = 1;
                    this.initInteractions();
                    requestAnimationFrame(this.render.bind(this));
                }
                setThinking(thinking) {
                    this.isThinking = thinking;
                    if (thinking) this.thinkingStartTime = Date.now();
                }
                setTyping(isTyping) { this.isTyping = isTyping; }
                wakeUp() { if (this.isSleeping) { this.isSleeping = false; this.blink(); } this.lastInteractionTime = Date.now(); }
                blink() {
                    if (this.isBlinking) return;
                    this.isBlinking = true;
                    this.lastBlinkTime = Date.now();
                    let startTime = null;
                    const duration = 150;
                    const animateBlink = (timestamp) => {
                        if (!startTime) startTime = timestamp;
                        const elapsed = timestamp - startTime;
                        if (elapsed < duration) { this.blinkProgress = 1 - (elapsed / duration); }
                        else if (elapsed < duration * 2) { this.blinkProgress = (elapsed - duration) / duration; }
                        else { this.blinkProgress = 1; this.isBlinking = false; return; }
                        requestAnimationFrame(animateBlink);
                    };
                    requestAnimationFrame(animateBlink);
                }
                initInteractions() {
                    const onMove = (e) => {
                        if (this.isSleeping || this.isTyping || this.isThinking) return;
                        this.wakeUp();
                        const max = 15;
                        const bcr = document.body.getBoundingClientRect();
                        const clientX = e.clientX || (e.touches && e.touches[0] ? e.touches[0].clientX : 0);
                        const clientY = e.clientY || (e.touches && e.touches[0] ? e.touches[0].clientY : 0);
                        this.targetLookX = (clientX / bcr.width - 0.5) * 2 * max;
                        this.targetLookY = (clientY / bcr.height - 0.5) * 2 * max;
                    };
                    window.addEventListener('mousemove', onMove);
                    window.addEventListener('touchmove', onMove, { passive: true });
                }
                render() {
                    this.lookX += (this.targetLookX - this.lookX) * 0.1;
                    this.lookY += (this.targetLookY - this.lookY) * 0.1;
                    this.ctx.clearRect(0, 0, this.W, this.H);
                    this.drawFaceplate();
                    const now = Date.now();
                    if (this.isSleeping) {
                        this.drawSleepEyes();
                    } else {
                        const isIdle = !this.isThinking && !this.isTyping && !this.isBlinking;
                        if (this.isThinking) {
                            const elapsed = now - this.thinkingStartTime;
                            const oscillation = Math.sin(elapsed / 300);
                            const scaleAmount = 0.3;
                            this.leftEyeScale = 1 - (scaleAmount * (oscillation + 1) / 2);
                            this.rightEyeScale = 1 - (scaleAmount * (-oscillation + 1) / 2);
                        } else {
                            this.leftEyeScale = 1; this.rightEyeScale = 1;
                        }
                        if (isIdle && now - this.lastBlinkTime > 3000 + Math.random() * 2000) {
                            this.blink();
                        }
                        this.drawExpression();
                    }
                    requestAnimationFrame(this.render.bind(this));
                }
                drawFaceplate() {
                    const c = this.ctx;
                    const r = 60;
                    c.fillStyle = '#030712';
                    c.strokeStyle = '#00eaff';
                    c.lineWidth = 3.5;
                    c.beginPath();
                    c.roundRect(0, 0, this.W, this.H, r);
                    c.fill();
                    c.stroke();
                }
                drawExpression() {
                    const eW = 100; const eH = 100; const eR = 30;
                    const eY = this.H / 2 + this.lookY;
                    const lCX = this.W * 0.3 + this.lookX;
                    const rCX = this.W * 0.7 + this.lookX;
                    const lW = eW * this.leftEyeScale;
                    const lH = eH * this.blinkProgress * this.leftEyeScale;
                    const rW = eW * this.rightEyeScale;
                    const rH = eH * this.blinkProgress * this.rightEyeScale;
                    this.drawEye(lCX, eY, lW, lH, eR);
                    this.drawEye(rCX, eY, rW, rH, eR);
                }
                drawEye(cx, cy, w, h, r) {
                    const c = this.ctx;
                    c.shadowBlur = 25;
                    c.shadowColor = 'rgba(0, 234, 255, 0.7)';
                    c.fillStyle = '#00eaff';
                    c.beginPath();
                    c.roundRect(cx - w / 2, cy - h / 2, w, h, r);
                    c.fill();
                    c.shadowBlur = 0;
                    c.globalCompositeOperation = 'source-atop';
                    c.fillStyle = 'rgba(0,0,0,0.35)';
                    for (let i = 0; i < h; i += 3) {
                        c.fillRect(cx - w / 2, cy - h / 2 + i, w, 1.5);
                    }
                    c.globalCompositeOperation = 'source-over';
                }
                drawSleepEyes() {
                    const c = this.ctx;
                    const y = this.H / 2; const w = 100; const h = 12; const r = h / 2;
                    c.fillStyle = '#00eaff';
                    c.shadowColor = 'rgba(0, 234, 255, 0.7)';
                    c.shadowBlur = 15;
                    c.beginPath();
                    c.roundRect(this.W * 0.3 - w / 2, y - h / 2, w, h, r);
                    c.roundRect(this.W * 0.7 - w / 2, y - h / 2, w, h, r);
                    c.fill();
                    c.shadowBlur = 0;
                }
            }
            let robot;
            document.addEventListener('DOMContentLoaded', () => {
                const canvas = document.getElementById('faceCanvas');
                robot = new RobotFace(canvas);
            });
            function updateThinking(thinking) {
                if (robot) robot.setThinking(thinking);
            }
            </script>
        </body>
        </html>
    """.trimIndent()

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(0x00000000)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.evaluateJavascript("updateThinking($isThinking);", null)
            }
        )
    }
}

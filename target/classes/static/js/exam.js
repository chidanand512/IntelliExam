let tabWarnings = 0;
let visitedQuestions = new Set();
let examSubmitted = false;
let examStarted = false;
let timerHandle;

function setupExam(durationMinutes, strictFullscreen = true) {
    setupQuestionTracking();
    setupTabSwitchPolicy();
    setupSubmitState();
    setupSecureStart(durationMinutes, strictFullscreen);
}

function setupSecureStart(durationMinutes, strictFullscreen) {
    const startButton = document.getElementById('enterFullScreenBtn');
    const overlay = document.getElementById('secureStartOverlay');
    const fullscreenStatus = document.getElementById('fullscreenStatus');
    const fullscreenError = document.getElementById('fullscreenError');
    if (!startButton || !overlay) {
        setupTimer(durationMinutes * 60);
        return;
    }

    startButton.addEventListener('click', async () => {
        startButton.disabled = true;
        startButton.innerHTML = '<i class="bi bi-arrows-fullscreen"></i> Opening full screen...';
        if (fullscreenError) {
            fullscreenError.classList.add('d-none');
        }
        await requestFullScreen();
        await waitForFullScreen();
        if (strictFullscreen && !isFullScreenActive()) {
            startButton.disabled = false;
            startButton.innerHTML = '<i class="bi bi-arrows-fullscreen"></i> Try Full Screen Again';
            if (fullscreenError) {
                fullscreenError.classList.remove('d-none');
            }
            return;
        }
        examStarted = true;
        overlay.classList.add('hidden');
        if (fullscreenStatus) {
            fullscreenStatus.textContent = 'Active';
        }
        setupTimer(durationMinutes * 60);
    });
}

function requestFullScreen() {
    const el = document.documentElement;
    if (el.requestFullscreen) {
        return el.requestFullscreen({ navigationUI: 'hide' }).catch(() => {});
    }
    if (el.webkitRequestFullscreen) {
        el.webkitRequestFullscreen();
        return Promise.resolve();
    }
    if (el.msRequestFullscreen) {
        el.msRequestFullscreen();
        return Promise.resolve();
    }
    return Promise.resolve();
}

function isFullScreenActive() {
    return Boolean(document.fullscreenElement || document.webkitFullscreenElement || document.msFullscreenElement);
}

function waitForFullScreen() {
    return new Promise((resolve) => setTimeout(resolve, 250));
}

function setupTimer(seconds) {
    const timerEl = document.getElementById('timer');
    const form = document.getElementById('examForm');
    const tick = () => {
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        timerEl.textContent = `${m}:${s.toString().padStart(2, '0')}`;
        if (seconds <= 0) {
            clearInterval(timerHandle);
            form.submit();
            return;
        }
        seconds--;
    };
    tick();
    timerHandle = setInterval(tick, 1000);
}

function setupQuestionTracking() {
    const radios = document.querySelectorAll('input[type="radio"]');
    const summary = document.getElementById('summary');
    const update = () => {
        const questionIds = new Set();
        const attended = new Set();
        radios.forEach((r) => {
            questionIds.add(r.name);
            if (r.checked) attended.add(r.name);
        });
        const total = questionIds.size;
        const done = attended.size;
        const notVisited = total - visitedQuestions.size;
        const skipped = Math.max(visitedQuestions.size - done, 0);
        summary.textContent = `Attended: ${done} | Skipped: ${skipped} | Not Visited: ${notVisited}`;
    };
    radios.forEach((r) => {
        r.addEventListener('focus', () => {
            visitedQuestions.add(r.name);
            update();
        });
        r.addEventListener('change', () => {
            visitedQuestions.add(r.name);
            update();
        });
    });
    update();
}

function setupTabSwitchPolicy() {
    const warningCount = document.getElementById('warningCount');
    const recordViolation = (message) => {
        if (examSubmitted) {
            return;
        }
        tabWarnings++;
        if (warningCount) {
            warningCount.textContent = `${Math.min(tabWarnings, 2)}/2`;
        }
        if (tabWarnings < 3) {
            alert(`${message} Warning ${tabWarnings}/2.`);
        }
        if (tabWarnings >= 3) {
            alert('Third violation detected. Exam is auto-submitted.');
            examSubmitted = true;
            document.getElementById('examForm').submit();
        }
    };

    document.addEventListener('visibilitychange', () => {
        if (examStarted && document.hidden) {
            recordViolation('Tab switching is not allowed.');
        }
    });

    document.addEventListener('fullscreenchange', () => {
        const fullscreenStatus = document.getElementById('fullscreenStatus');
        if (fullscreenStatus) {
            fullscreenStatus.textContent = isFullScreenActive() ? 'Active' : 'Exited';
        }
        if (examStarted && !isFullScreenActive()) {
            recordViolation('Full-screen mode was exited.');
        }
    });
}

function setupSubmitState() {
    const form = document.getElementById('examForm');
    if (form) {
        form.addEventListener('submit', () => {
            examSubmitted = true;
        });
    }
}

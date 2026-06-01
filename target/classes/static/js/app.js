function initTheme() {
    const saved = localStorage.getItem('theme') || 'dark';
    if (saved === 'dark') {
        document.body.classList.add('theme-dark');
    }
    syncThemeButtons();
}

function toggleTheme() {
    document.body.classList.toggle('theme-dark');
    const dark = document.body.classList.contains('theme-dark');
    localStorage.setItem('theme', dark ? 'dark' : 'light');
    syncThemeButtons();
}

function syncThemeButtons() {
    const dark = document.body.classList.contains('theme-dark');
    document.querySelectorAll('.theme-toggle-btn i, [aria-label="Toggle theme"] i').forEach((icon) => {
        icon.className = dark ? 'bi bi-sun' : 'bi bi-moon-stars';
    });
}

function toggleSideNav() {
    const nav = document.getElementById('sidenav');
    if (nav) {
        nav.classList.toggle('open');
    }
}

function closeSideNavOnNavigate() {
    document.querySelectorAll('.sidenav-menu a').forEach((a) => {
        a.addEventListener('click', () => {
            if (window.innerWidth <= 992) {
                document.getElementById('sidenav')?.classList.remove('open');
            }
        });
    });
}

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    closeSideNavOnNavigate();
});

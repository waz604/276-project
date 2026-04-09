// i used copilot to help me debugging with the theme flash logic not working
(function () {
    var isDark = localStorage.getItem('studbuds-theme') === 'night';
    document.documentElement.setAttribute('data-theme', isDark ? 'night' : 'silk');
})();

function _applyThemeClasses(theme) {
    var isDark = theme === 'night';
    document.querySelectorAll('[data-day-classes]').forEach(function (el) {
        var dayClasses = el.getAttribute('data-day-classes').split(' ').filter(Boolean);
        var nightClasses = el.getAttribute('data-night-classes').split(' ').filter(Boolean);
        if (isDark) {
            dayClasses.forEach(function (c) { el.classList.remove(c); });
            nightClasses.forEach(function (c) { el.classList.add(c); });
        } else {
            nightClasses.forEach(function (c) { el.classList.remove(c); });
            dayClasses.forEach(function (c) { el.classList.add(c); });
        }
    });
}



document.addEventListener('DOMContentLoaded', function () {
    var isDark = localStorage.getItem('studbuds-theme') === 'night';
    var theme = isDark ? 'night' : 'silk';
       


    // restore
    document.querySelectorAll('.theme-controller').forEach(function (el) {
        el.checked = isDark;
    });

    _applyThemeClasses(theme);

    // sync
    document.addEventListener('change', function (e) {
        if (e.target.classList.contains('theme-controller')) {
            var dark = e.target.checked;
            var newTheme = dark ? 'night' : 'silk';
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('studbuds-theme', newTheme);
            _applyThemeClasses(newTheme);
        }
    });
});


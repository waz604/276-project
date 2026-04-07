// ─────────────────────────────────────────────────────────────────────────────
// XP System
// Drop this script into any page that needs give_XP().
// It self-injects a fixed overlay bar at the bottom of the viewport.
// No HTML required -- just <script src="/XP_system/XP.js"></script>.
// ─────────────────────────────────────────────────────────────────────────────

var CurrXp      = 0;
let CurrentLvl  = 1;
let XpToNextLvl = 100; // initialised to level 1 requirement; updated via lvl_checker()
let totalXP     = 0;

const MAX_LEVEL = 20;

// ── Timing constants (ms) ─────────────────────────────────────────────────────
const FILL_TO_100_MS  = 900;   // bar animates to 100%
const BOUNCE_MS       = 550;   // squash-and-stretch
const LEVELUP_HOLD_MS = 1800;  // gold bar fully visible -- LEVEL UP text shows
const SNAP_DELAY_MS   = 250;   // pause at 0% before filling new level
const HIDE_DELAY_MS   = 2800;  // idle time before bar slides away

// ── Self-inject the overlay bar ───────────────────────────────────────────────
(function injectXpBar() {
    if (document.getElementById('xp-overlay')) return;

    const overlay = document.createElement('div');
    overlay.id = 'xp-overlay';
    overlay.innerHTML = `
        <div id="xp-bar-wrap">
            <div id="xp-level-badge">1</div>
            <div id="xp-track">
                <div id="xp-fill"></div>
                <span id="myXP">0 / 100</span>
            </div>
            <div id="xp-level-next">2</div>
        </div>
        <div id="xp-total-label">Total XP: <span id="totXP">0</span></div>
    `;
    document.body.appendChild(overlay);

    // Seed bar from the server once the DOM node exists
    _initFromServer();
})();

// ── Server sync ───────────────────────────────────────────────────────────────

// On page load: fetch the user's saved total XP, derive level/progress from it,
// and silently apply it to the bar state without any animation.
function _initFromServer() {
    fetch('/xp/total')
        .then(function(res) {
            if (!res.ok) return; // not logged in, leave bar at defaults
            return res.json();
        })
        .then(function(data) {
            if (!data || data.error) return;

            var saved = data.totalXp || 0;
            if (saved <= 0) return;

            totalXP = saved;
            document.getElementById('totXP').textContent = totalXP;

            // Walk the level curve to find CurrentLvl and CurrXp
            var remaining = saved;
            CurrentLvl  = 1;
            while (CurrentLvl < MAX_LEVEL) {
                var needed = lvl_checker(CurrentLvl);
                if (remaining < needed) break;
                remaining -= needed;
                CurrentLvl++;
            }

            if (CurrentLvl >= MAX_LEVEL) {
                // Permanently at max
                CurrentLvl  = MAX_LEVEL;
                XpToNextLvl = lvl_checker(MAX_LEVEL);
                CurrXp      = XpToNextLvl;

                var fill = document.getElementById('xp-fill');
                fill.classList.add('xp-fill--max-level');
                fill.style.transition = 'none';
                fill.style.width      = '100%';

                var myXP = document.getElementById('myXP');
                myXP.classList.add('xp-text--maxlevel');
                myXP.textContent = '✦ MAX LEVEL ✦';

                document.getElementById('xp-level-badge').textContent = 'MAX';
                document.getElementById('xp-level-badge').classList.add('xp-badge--gold');
                document.getElementById('xp-level-next').textContent  = '★';
                document.getElementById('xp-level-next').classList.add('xp-badge--gold');
            } else {
                CurrXp      = remaining;
                XpToNextLvl = lvl_checker(CurrentLvl);

                var pct = Math.min((CurrXp / XpToNextLvl) * 100, 100);
                var fill = document.getElementById('xp-fill');
                fill.style.transition = 'none';
                fill.style.width      = pct + '%';

                document.getElementById('myXP').textContent           = CurrXp + ' / ' + XpToNextLvl;
                document.getElementById('xp-level-badge').textContent = CurrentLvl;
                document.getElementById('xp-level-next').textContent  = CurrentLvl + 1;
            }
        })
        .catch(function() {}); // silently ignore network errors
}

// Posts an XP award to the server so it persists in the database.
// Called internally by give_XP
function _saveXpToServer(amount) {
    var body = new URLSearchParams();
    body.append('amount', amount);

    fetch('/xp/award', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()
    }).catch(function() {});
}

// ── Core XP logic ─────────────────────────────────────────────────────────────

function give_XP(amount) {
    // At max level: accumulate totalXP and briefly show the bar, but don't touch the fill
    if (CurrentLvl >= MAX_LEVEL) {
        totalXP += amount;
        _saveXpToServer(amount);
        document.getElementById('totXP').textContent = totalXP;
        _spawnFloater('+' + amount + ' XP');

        // Slide the bar up so the player sees the updated total XP
        const overlay = document.getElementById('xp-overlay');
        if (!_isVisible) {
            overlay.classList.remove('xp-overlay--hidden');
            overlay.classList.add('xp-overlay--visible');
            _isVisible = true;
        }
        if (!_levelUpLock) {
            clearTimeout(_hideTimer);
            _hideTimer = setTimeout(_hideBar, HIDE_DELAY_MS);
        }
        return;
    }

    const prevXp = CurrXp;

    CurrXp  += amount;
    totalXP += amount;

    // Persist to database
    _saveXpToServer(amount);

    // Update total XP display immediately
    document.getElementById('totXP').textContent = totalXP;

    // Safety check — clamp to MAX_LEVEL if anything is already overflowed
    max_lvl_checker();
    let simXp   = CurrXp;
    let simLvl  = CurrentLvl;
    let simNext = XpToNextLvl;
    let levelsGained = 0;

    while (simXp >= simNext && simLvl < MAX_LEVEL) {
        simXp -= simNext;
        simLvl++;
        simNext = lvl_checker(simLvl);
        levelsGained++;
    }

    const reachesMax = (simLvl >= MAX_LEVEL);
    if (reachesMax) {
        // At max level the bar sits permanently at 100%
        simXp   = simNext;
    }

    if (levelsGained > 0) {
        _animateLevelUpSequence(prevXp, levelsGained, simLvl, simXp, simNext, reachesMax);
    } else {
        // Normal gain — no level-up
        CurrXp = simXp;
        change_Percent();
        document.getElementById('myXP').textContent = CurrXp + ' / ' + XpToNextLvl;
        _animateXpGain(prevXp, amount);
    }
}

function change_Percent() {
    const pct = Math.min((CurrXp / XpToNextLvl) * 100, 100);
    document.getElementById('xp-fill').style.width = pct + '%';
    const legacy = document.querySelector('.XpBackgroundReactive');
    if (legacy) legacy.style.width = pct + '%';
}

// Safety net: clamps level and XP state to MAX_LEVEL if anything overflows.
// Call this any time level state changes.
function max_lvl_checker() {
    if (CurrentLvl > MAX_LEVEL) {
        console.warn('[XP] Level overflow detected (' + CurrentLvl + '). Resetting to ' + MAX_LEVEL + '.');
        CurrentLvl  = MAX_LEVEL;
        XpToNextLvl = lvl_checker(MAX_LEVEL);
        CurrXp      = XpToNextLvl; // full bar at max
    }
    if (CurrentLvl === MAX_LEVEL && CurrXp > XpToNextLvl) {
        CurrXp = XpToNextLvl;
    }
}

// Returns the XP required to level up FROM the given level.
// e.g. lvl_checker(1) returns 100 means you need 100 XP while at level 1 to reach level 2.
function lvl_checker(level) {
    switch (level) {
        case  1: return 100;
        case  2: return 150;
        case  3: return 220;
        case  4: return 300;
        case  5: return 400;
        case  6: return 520;
        case  7: return 670;
        case  8: return 850;
        case  9: return 1050;
        case 10: return 1200;
        case 11: return 1450;
        case 12: return 1700;
        case 13: return 2000;
        case 14: return 2200;
        case 15: return 2500;
        case 16: return 2850;
        case 17: return 3200;
        case 18: return 3550;
        case 19: return 3800;
        case 20: return 4000;
        default: return 4000; // clamp at max
    }
}

// ── Multi-level animation engine ──────────────────────────────────────────────

let _hideTimer   = null;
let _isVisible   = false;
let _levelUpLock = false;

function _animateLevelUpSequence(prevXp, levelsGained, finalLvl, finalXp, finalNext, reachesMax) {
    _levelUpLock = true;
    clearTimeout(_hideTimer);

    const overlay = document.getElementById('xp-overlay');
    const fill    = document.getElementById('xp-fill');

    // Slide bar in if hidden
    if (!_isVisible) {
        overlay.classList.remove('xp-overlay--hidden');
        overlay.classList.add('xp-overlay--visible');
        _isVisible = true;

        fill.style.transition = 'none';
        fill.style.width      = Math.min((prevXp / XpToNextLvl) * 100, 100) + '%';
        void fill.offsetWidth;
    }

    // Show a floater indicating how many levels gained
    _spawnFloater(levelsGained > 1 ? '+' + levelsGained + ' levels!' : 'LEVEL UP!');

    _doOneLevelUp(0, levelsGained, finalLvl, finalXp, finalNext, reachesMax);
}

function _doOneLevelUp(stepIndex, totalLevels, finalLvl, finalXp, finalNext, reachesMax) {
    const fill = document.getElementById('xp-fill');
    const myXP = document.getElementById('myXP');

    const isLastStep  = (stepIndex === totalLevels - 1);
    const isMaxAtStep = isLastStep && reachesMax;

    // Step A — fill to 100%
    fill.style.transition = `width ${FILL_TO_100_MS}ms cubic-bezier(0.33, 1, 0.68, 1)`;
    fill.style.width      = '100%';

    setTimeout(() => {
        // Step B — bounce
        fill.classList.add('xp-fill--levelup-bounce');

        setTimeout(() => {
            // Step C , freeze gold, show LEVEL UP text inside the bar.
            // We don't know if this is max yet (increment hasn't happened),
            // so start with the generic levelup-hold; nowAtMax check below
            // will immediately upgrade to max-level if needed.
            fill.classList.remove('xp-fill--levelup-bounce');
            fill.classList.add('xp-fill--levelup-hold');

            // Badges go gold
            document.getElementById('xp-level-badge').classList.add('xp-badge--gold');
            document.getElementById('xp-level-next').classList.add('xp-badge--gold');

            // Placeholder text, will be overwritten below once we know if max
            myXP.classList.add('xp-text--levelup');
            myXP.textContent = '⬆ LEVEL UP!';

            // Advance the game state by one level
            CurrentLvl++;
            // Hard clamp, never let CurrentLvl exceed MAX_LEVEL
            if (CurrentLvl >= MAX_LEVEL) {
                CurrentLvl  = MAX_LEVEL;
                XpToNextLvl = lvl_checker(MAX_LEVEL);
                CurrXp      = XpToNextLvl;
            } else {
                CurrXp      = 0;
                XpToNextLvl = lvl_checker(CurrentLvl);
            }
            max_lvl_checker();

            // Re-derive max status from actual state,  more reliable than the
            // pre-computed isMaxAtStep flag which can be wrong on overflow paths.
            const nowAtMax = (CurrentLvl >= MAX_LEVEL);

            // Update badge numbers to new level
            if (nowAtMax) {
                document.getElementById('xp-level-badge').textContent = 'MAX';
                document.getElementById('xp-level-next').textContent  = '★';
                // Switch bar and text to permanent max-level style immediately
                myXP.classList.remove('xp-text--levelup');
                myXP.classList.add('xp-text--maxlevel');
                myXP.textContent = '✦ MAX LEVEL ✦';
                fill.classList.remove('xp-fill--levelup-hold');
                fill.classList.add('xp-fill--max-level');
                fill.style.width = '100%';
            } else {
                document.getElementById('xp-level-badge').textContent = CurrentLvl;
                document.getElementById('xp-level-next').textContent  = CurrentLvl + 1;
            }

            // Pop both badges
            ['xp-level-badge', 'xp-level-next'].forEach(id => {
                const el = document.getElementById(id);
                if (!el) return;
                el.classList.remove('xp-badge--pop');
                void el.offsetWidth;
                el.classList.add('xp-badge--pop');
                setTimeout(() => el.classList.remove('xp-badge--pop'), 600);
            });

            // ─────────────────────────────────────────────────────────────────
            if (nowAtMax) {
                // MAX LEVEL reached,golden bar stays permanently ──────────
                setTimeout(() => {
                    // Re-enforce permanent state after hold 
                    fill.classList.remove('xp-fill--levelup-hold');
                    fill.classList.add('xp-fill--max-level');
                    fill.style.transition = 'none';
                    fill.style.width      = '100%';

                    myXP.classList.remove('xp-text--levelup');
                    myXP.classList.add('xp-text--maxlevel');
                    myXP.textContent = '✦ MAX LEVEL ✦';

                    _levelUpLock = false;
                    clearTimeout(_hideTimer);
                    _hideTimer = setTimeout(_hideBar, HIDE_DELAY_MS);
                }, LEVELUP_HOLD_MS);

            } else if (isLastStep) {
                // Last level-up (not max), snap to new level's resting XP ──
                setTimeout(() => {
                    myXP.classList.remove('xp-text--levelup');

                    fill.classList.remove('xp-fill--levelup-hold');
                    document.getElementById('xp-level-badge').classList.remove('xp-badge--gold');
                    document.getElementById('xp-level-next').classList.remove('xp-badge--gold');

                    // Apply final state
                    CurrXp      = finalXp;
                    XpToNextLvl = finalNext;

                    fill.style.transition = 'none';
                    fill.style.width      = '0%';
                    void fill.offsetWidth;

                    myXP.textContent = CurrXp + ' / ' + XpToNextLvl;
                    document.getElementById('xp-level-badge').textContent = CurrentLvl;
                    document.getElementById('xp-level-next').textContent  = CurrentLvl + 1;

                    setTimeout(() => {
                        const newPct = Math.min((CurrXp / XpToNextLvl) * 100, 100);
                        fill.style.transition = 'width 0.7s cubic-bezier(0.34, 1.2, 0.64, 1)';
                        fill.style.width      = newPct + '%';

                        _levelUpLock = false;
                        clearTimeout(_hideTimer);
                        _hideTimer = setTimeout(_hideBar, HIDE_DELAY_MS);
                    }, SNAP_DELAY_MS);

                }, LEVELUP_HOLD_MS);

            } else {
                // More levels to animate, chain to next step ─────────────────
                setTimeout(() => {
                    myXP.classList.remove('xp-text--levelup');
                    fill.classList.remove('xp-fill--levelup-hold');
                    document.getElementById('xp-level-badge').classList.remove('xp-badge--gold');
                    document.getElementById('xp-level-next').classList.remove('xp-badge--gold');

                    fill.style.transition = 'none';
                    fill.style.width      = '0%';
                    void fill.offsetWidth;

                    setTimeout(() => {
                        _doOneLevelUp(stepIndex + 1, totalLevels, finalLvl, finalXp, finalNext, reachesMax);
                    }, SNAP_DELAY_MS);

                }, LEVELUP_HOLD_MS);
            }

        }, BOUNCE_MS);

    }, FILL_TO_100_MS);
}

// Normal (no level-up) animation, slides bar in and fills to new percent
function _animateXpGain(prevXp, amount) {
    const overlay = document.getElementById('xp-overlay');
    const fill    = document.getElementById('xp-fill');

    if (!_isVisible) {
        overlay.classList.remove('xp-overlay--hidden');
        overlay.classList.add('xp-overlay--visible');
        _isVisible = true;

        fill.style.transition = 'none';
        fill.style.width      = Math.min((prevXp / XpToNextLvl) * 100, 100) + '%';
        void fill.offsetWidth;
    }

    _spawnFloater('+' + amount + ' XP');

    const targetPct = Math.min((CurrXp / XpToNextLvl) * 100, 100);
    fill.style.transition = targetPct >= 80
        ? 'width 1.1s cubic-bezier(0.08, 0.82, 0.17, 1)'
        : 'width 0.6s cubic-bezier(0.34, 1.4, 0.64, 1)';
    fill.style.width = targetPct + '%';

    if (!_levelUpLock) {
        clearTimeout(_hideTimer);
        _hideTimer = setTimeout(_hideBar, HIDE_DELAY_MS);
    }
}

function _hideBar() {
    if (_levelUpLock) return;
    const overlay = document.getElementById('xp-overlay');
    overlay.classList.remove('xp-overlay--visible');
    overlay.classList.add('xp-overlay--hidden');
    _isVisible = false;
}

function _spawnFloater(text) {
    const wrap    = document.getElementById('xp-bar-wrap');
    const floater = document.createElement('div');
    floater.className   = 'xp-floater';
    floater.textContent = text;
    wrap.appendChild(floater);
    setTimeout(() => floater.remove(), 1100);
}

// ── Legacy stubs ──────────────────────────────────────────────────────────────
function triggerXpGainAnimation() {}
function triggerLevelUpAnimation() {}

// display_XP(amount)
// Identical to give_XP() but does NOT call _saveXpToServer().
// Use this when the server has already persisted the XP (e.g. after a
// successful POST to /xp/study-session or /xp/time-challenge). calling
// give_XP() in those cases would double-count the XP in the database.
function display_XP(amount) {
    if (amount <= 0) return;

    if (CurrentLvl >= MAX_LEVEL) {
        totalXP += amount;
        document.getElementById('totXP').textContent = totalXP;
        _spawnFloater('+' + amount + ' XP');

        const overlay = document.getElementById('xp-overlay');
        if (!_isVisible) {
            overlay.classList.remove('xp-overlay--hidden');
            overlay.classList.add('xp-overlay--visible');
            _isVisible = true;
        }
        if (!_levelUpLock) {
            clearTimeout(_hideTimer);
            _hideTimer = setTimeout(_hideBar, HIDE_DELAY_MS);
        }
        return;
    }

    const prevXp = CurrXp;

    CurrXp  += amount;
    totalXP += amount;

    document.getElementById('totXP').textContent = totalXP;

    max_lvl_checker();
    let simXp   = CurrXp;
    let simLvl  = CurrentLvl;
    let simNext = XpToNextLvl;
    let levelsGained = 0;

    while (simXp >= simNext && simLvl < MAX_LEVEL) {
        simXp -= simNext;
        simLvl++;
        simNext = lvl_checker(simLvl);
        levelsGained++;
    }

    const reachesMax = (simLvl >= MAX_LEVEL);
    if (reachesMax) simXp = simNext;

    if (levelsGained > 0) {
        _animateLevelUpSequence(prevXp, levelsGained, simLvl, simXp, simNext, reachesMax);
    } else {
        CurrXp = simXp;
        change_Percent();
        document.getElementById('myXP').textContent = CurrXp + ' / ' + XpToNextLvl;
        _animateXpGain(prevXp, amount);
    }
}
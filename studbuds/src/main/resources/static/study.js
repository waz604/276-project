const cards = JSON.parse(document.getElementById('flashcard').dataset.cards);
let index = 0;

// Track session start time for XP calculation
const studyStartTime = Date.now();
let xpAlreadyAwarded = false;

// DOM elements
const questionText = document.getElementById('questionText'); 
const answerText   = document.getElementById('answerText'); 
const total        = document.getElementById('total'); 
const prevBtn      = document.getElementById('previous'); 
const nextBtn      = document.getElementById('next'); 
const flipBtn      = document.getElementById('flip');
const innerCard    = document.getElementById('inner-card');
const finishBtn    = document.getElementById('finishBtn');
const xpMsg        = document.getElementById('xpMsg');

// Render card's content
function renderCard() { 
    questionText.textContent = cards[index].question;
    answerText.style.visibility = 'hidden';
    answerText.textContent = cards[index].answer; 
    total.textContent = (index + 1) + ' / ' + cards.length; 
    prevBtn.style.display = (index === 0) ? 'none' : 'block'; 
    nextBtn.style.display = (index === cards.length - 1) ? 'none' : 'block';
    innerCard.classList.remove('is-flipped');

    // Show the Finish button once the user reaches the last card
    if (index === cards.length - 1 && !xpAlreadyAwarded) {
        finishBtn.style.display = 'inline-block';
    }
}
    
// Flip card on click
function flipCard() {
    answerText.style.visibility = 'visible';
    innerCard.classList.toggle('is-flipped');
}

prevBtn.addEventListener('click', () => {
    if (index > 0) {
        index--;
        renderCard();
    }
});

nextBtn.addEventListener('click', () => {
    if (index < cards.length - 1) {
        index++;
        renderCard();
    }
});

flip.addEventListener('click', () => {
    flipCard();
});

// ── XP awarding ───────────────────────────────────────────────────────────────

finishBtn.addEventListener('click', () => {
    if (xpAlreadyAwarded) return;
    xpAlreadyAwarded = true;
    finishBtn.disabled = true;
    finishBtn.textContent = '…';

    const timeSeconds = Math.round((Date.now() - studyStartTime) / 1000);
    const totalCards  = cards.length;
    const deckId      = finishBtn.dataset.deckId;

    const body = new URLSearchParams();
    body.append('totalCards',  totalCards);
    body.append('timeSeconds', timeSeconds);
    body.append('deckId',      deckId);

    fetch('/xp/study-session', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()
    })
    .then(res => res.json())
    .then(data => {
        finishBtn.style.display = 'none';
        xpMsg.style.display = 'block';

        if (data.xp > 0) {
            xpMsg.textContent = '+' + data.xp + ' XP earned!';
            xpMsg.style.color = '#4caf50';
            // Animate the XP bar without double-saving (server already saved it)
            if (typeof display_XP === 'function') display_XP(data.xp);
        } else {
            xpMsg.textContent = 'No XP this time (already studied recently, or too fast).';
            xpMsg.style.color = '#888';
        }
    })
    .catch(() => {
        xpMsg.style.display = 'block';
        xpMsg.textContent   = 'Could not save XP — are you logged in?';
        xpMsg.style.color   = '#c00';
        finishBtn.style.display = 'none';
    });
});

renderCard();
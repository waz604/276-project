const cards = JSON.parse(document.getElementById('study-container').dataset.cards);
let index = 0;

// Track session start time for XP calculation
const studyStartTime = Date.now();
let xpAlreadyAwarded = false;

// DOM elements
const questionText = document.getElementById('questionText'); 
const answerText = document.getElementById('answerText'); 
const indexDisplay = document.getElementById('indexBadge'); 
const prevBtn = document.getElementById('prevBtn'); 
const nextBtn = document.getElementById('nextBtn');
const innerCard = document.getElementById('study-container');
const flipToggle = document.getElementById('flipToggle');
const finishBtn = document.getElementById('finishBtn');
const xpMsg = document.getElementById('xpMsg');

// Render card's content
function renderCard() { 
    flipToggle.checked = false;
    
    questionText.textContent = cards[index].question;
    answerText.textContent = cards[index].answer; 
    indexDisplay.textContent = (index+1) + ' / ' + cards.length; 
}

prevBtn.addEventListener('click', () => {
    if(index > 0) {
        index--;
        renderCard();
    } else {
        index = cards.length-1;
        renderCard();
    }
})

nextBtn.addEventListener('click', () => {
    if(index < cards.length-1) {
        index++;
        renderCard();
    } else {
        index = 0;
        renderCard();
    }
    // Show the Finish button once the user reaches the last card
    if (index === cards.length - 1 && !xpAlreadyAwarded) {
        finishBtn.style.display = 'inline-block';
    }
})

// calculate_XP(flashcard_num, time, correct cards)

//  XP awarding 

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
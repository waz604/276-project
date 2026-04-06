const cards = JSON.parse(document.getElementById('study-container').dataset.cards);
let index = 0;

// DOM elements
const questionText = document.getElementById('questionText'); 
const answerText = document.getElementById('answerText'); 
const indexDisplay = document.getElementById('indexBadge'); 
const prevBtn = document.getElementById('prevBtn'); 
const nextBtn = document.getElementById('nextBtn');
const innerCard = document.getElementById('study-container');
const flipToggle = document.getElementById('flipToggle');

// Render card's content
function renderCard() { 
    flipToggle.checked = false;
    
    questionText.textContent = cards[index].question;
    answerText.textContent = cards[index].answer; 
    indexDisplay.textContent = (index+1) + ' / ' + cards.length; 
    prevBtn.style.display = (index === 0) ? 'none' : 'block'; 
    nextBtn.style.display = (index === cards.length - 1) ? 'none' : 'block';
}

prevBtn.addEventListener('click', () => {
    if(index > 0) {
        index--;
        renderCard();
    }
})

nextBtn.addEventListener('click', () => {
    if(index < cards.length-1) {
        index++;
        renderCard();
    }
})

// calculate_XP(flashcard_num, time, correct cards)

renderCard();
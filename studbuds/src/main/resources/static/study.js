const cards = JSON.parse(document.getElementById('flashcard').dataset.cards);
let index = 0;

// DOM elements
const questionText = document.getElementById('questionText'); 
const answerText = document.getElementById('answerText'); 
const total = document.getElementById('total'); 
const prevBtn = document.getElementById('previous'); 
const nextBtn = document.getElementById('next'); 
const flipBtn = document.getElementById('flip');
const innerCard = document.getElementById('inner-card');

// Render card's content
function renderCard() { 
    questionText.textContent = cards[index].question;
    answerText.style.visibility = 'hidden';
    answerText.textContent = cards[index].answer; 
    total.textContent = (index+1) + ' / ' + cards.length; 
    prevBtn.style.display = (index === 0) ? 'none' : 'block'; 
    nextBtn.style.display = (index === cards.length - 1) ? 'none' : 'block';
    innerCard.classList.remove('is-flipped');
}
    
// Flip card on click
function flipCard() {
    answerText.style.visibility = 'visible'
    innerCard.classList.toggle('is-flipped');
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

flip.addEventListener('click', () => {
    flipCard();
})
    
renderCard();

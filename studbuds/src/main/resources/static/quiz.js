const cards = JSON.parse(document.getElementById('flashcard').dataset.cards);

const questionText = document.getElementById('questionText'); 
const answerText = document.getElementById('answerText'); 
const indexDisplay = document.getElementById('progress'); 
const nextBtn = document.getElementById('nextBtn');
const progressBar = document.getElementById('progressBar');

let index = 0;

function renderCard() {
    questionText.textContent = cards[index].question;
    indexDisplay.textContent = (index+1) + ' / ' + cards.length; 
    prevBtn.style.display = (index === 0) ? 'none' : 'block'; 
    nextBtn.style.display = (index === cards.length - 1) ? 'none' : 'block';
}

nextBtn.addEventListener('click', () => {
    if(index < cards.length-1) {
        index++;
        progressBar.value = index+1;
        progressBar.max = cards.length;
        renderCard();
    }
})

renderCard();

const cards = JSON.parse(document.getElementById('flashcard').dataset.cards);

const questionText = document.getElementById('questionText'); 
const answerText = document.getElementById('answerText'); 
const indexDisplay = document.getElementById('progress');
const userAnswer = document.getElementById('userAnswer');
const submitAnswerBtn = document.getElementById('submitAnswer');
const skipBtn = document.getElementById('skipBtn');
const progressBar = document.getElementById('progressBar');

let index = 0;
let score = 0;

function renderCard() {
    questionText.textContent = cards[index].question;
    indexDisplay.textContent = (index+1) + ' / ' + cards.length;
    skipBtn.style.display = (index === cards.length - 1) ? 'none' : 'block';
}

function nextCard() {
    if(index < cards.length-1) {
        index++;
        progressBar.value = index+1;
        userAnswer.value = '';
        renderCard();
    } else {
        const message = `You reached the end of this deck!<br><br>
                         Your final score for this quiz is: 
                         <strong>${score}/${cards.length}<strong>
                         `;
        showResults(message)
    }
}

function showResults(message) {
    const resultModal = document.getElementById('final_results')
    const resultText = document.getElementById('result_text');
    resultText.innerHTML = message;
    resultModal.showModal();
}

function showAlert(message, type) {
    const result = document.getElementById("result");
    result.className = "alert";
    result.style.marginTop = "10px";

    if (type === "success") {
        result.classList.add("alert-success");
    } else if (type === "error") {
        result.classList.add("alert-error");
    } else {
        result.classList.add("alert-info");
    }

    result.innerHTML = message.replace(/\n/g, "<br>");
    result.classList.remove("hidden");
    setTimeout(() => {
            result.classList.add('hidden');
        }, 3000);
}

submitAnswerBtn.addEventListener('click', () => {
    const typed = userAnswer.value.trim().toLowerCase();
    const expected = cards[index].answer.trim().toLowerCase();

    if(typed === expected) {
        score++;
        const message = 'Your answer ' 
                        + '"' + userAnswer.value + '"'
                        + ' is correct!\n\n\n'
                        + 'Your current score is '
                        + score
                        + '/'
                        + cards.length;
        showAlert(message, "success");
        nextCard();
    } else {
        const message = 'Your answer ' 
                        + '"' + userAnswer.value + '"'
                        + ' is incorrect\n\n\n'
                        + 'Your current score is '
                        + score
                        + '/'
                        + cards.length;
        showAlert(message, "error");
        nextCard();
    }
})

skipBtn.addEventListener('click', nextCard)

renderCard();

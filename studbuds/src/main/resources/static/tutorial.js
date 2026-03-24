const flashcardBtn = document.querySelector('.flashcard-tutorial-btn');
const challengeBtn = document.querySelector('.challenge-tutorial-btn');
const XPBtn = document.querySelector('.XP-tutorial-btn');

const flashcardCard = document.querySelector('.tutorial-card-flashcards');
const challengeCard = document.querySelector('.tutorial-card-challenge');
const XPCard = document.querySelector('.tutorial-card-XP');

let slideIndex = 1;
showSlides(slideIndex);

// Next/previous controls
function plusSlides(n) {
  showSlides(slideIndex += n);
}

// Thumbnail image controls
function currentSlide(n) {
  showSlides(slideIndex = n);
}

function showSlides(n) {
    let i;

    let activeTutorial = document.querySelector('.is-active');
    if (!activeTutorial) return;

    let slides = activeTutorial.getElementsByClassName("mySlides");
    let dots = activeTutorial.getElementsByClassName("dot");

    if (n > slides.length) {slideIndex = 1}
    if (n < 1) {slideIndex = slides.length}

    let currentDisplay = activeTutorial.querySelector('.current-slide-num');
  let totalDisplay = activeTutorial.querySelector('.total-slides-num');

  // 2. Update the text to match the current index and total count
  if (currentDisplay) currentDisplay.textContent = slideIndex;
  if (totalDisplay) totalDisplay.textContent = slides.length;

    for (i = 0; i < slides.length; i++) {
        slides[i].style.display = "none";
    }

    for (i = 0; i < dots.length; i++) {
        dots[i].className = dots[i].className.replace(" active", "");
    }

    slides[slideIndex-1].style.display = "block";
    dots[slideIndex-1].className += " active";
}


function closeSlideshows() {
    flashcardCard.classList.remove('is-active');
    challengeCard.classList.remove('is-active');
    XPCard.classList.remove('is-active');
    
    flashcardBtn.classList.remove('button-activated');
    challengeBtn.classList.remove('button-activated');
    XPBtn.classList.remove('button-activated');
}

// Slideshow toggling
flashcardBtn.addEventListener('click', function() {
    const isActive = this.classList.contains('button-activated');
    
    closeSlideshows();
    
    if (!isActive) {
        flashcardCard.classList.add('is-active');
        this.classList.add('button-activated');

        slideIndex = 1;
        showSlides(slideIndex);
    }
});

challengeBtn.addEventListener('click', function() {
    const isActive = this.classList.contains('button-activated');
    
    closeSlideshows();
    
    if (!isActive) {
        challengeCard.classList.add('is-active');
        this.classList.add('button-activated');

        slideIndex = 1;
        showSlides(slideIndex);
    }
});

XPBtn.addEventListener('click', function() {
    const isActive = this.classList.contains('button-activated');
    
    closeSlideshows();
    
    if (!isActive) {
        XPCard.classList.add('is-active');
        this.classList.add('button-activated');

        slideIndex = 1;
        showSlides(slideIndex);
    }
});
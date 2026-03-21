const form       = document.getElementById('addForm');
const errorMsg   = document.getElementById('errorMsg');
const errorText  = document.getElementById('errorText');
const unameInput = document.getElementById('uname');
const pswInput   = document.getElementById('psw');

form.addEventListener('submit', function (e) {
    const uname = unameInput.value.trim();
    const psw   = pswInput.value.trim();

    let message = "";

    // MISSING FIELDS
    if (!uname || !psw) {
        e.preventDefault();

        if (!uname && !psw) {
            message = 'Please enter your username and password.';
        } else if (!uname) {
            message = 'Please enter your username.';
        } else {
            message = 'Please enter your password.';
        }
    }

    // INVALID PASSWORD LENGTH
    else if (psw.length < 8) {
        message = 'You password must be at least 8 characters.'
    }

    // PSW DOES NOT CONTAIN NUMBER
    else if (!psw.includes('0') ||
        !psw.includes('1') ||
        !psw.includes('2') ||
        !psw.includes('3') ||
        !psw.includes('4') ||
        !psw.includes('5') ||
        !psw.includes('6') ||
        !psw.includes('7') ||
        !psw.includes('8') ||
        !psw.includes('9') ||
        !psw.includes('10')) {
            message = 'You password must contain a number'
        }

    // PSW DOES NOT CONTAIN SPECIAL CHAR
    else if (!psw.includes('!') ||
        !psw.includes('@') ||
        !psw.includes('#') ||
        !psw.includes('$') ||
        !psw.includes('%')) {
            message = 'You password must contain a special character. (!@#$%)'
        }

    // If there is an error, display it depending on which field is incorrect
    if (message) {
        e.preventDefault();
        errorText.textContent = message;
        
        errorMsg.classList.remove('visible');
        void errorMsg.offsetWidth;
        errorMsg.classList.add('visible');
    }
});

[unameInput, pswInput].forEach(function (input) {
    input.addEventListener('input', function () {
        errorMsg.classList.remove('visible');
    });
});
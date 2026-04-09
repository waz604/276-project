const form       = document.getElementById('addForm');
const errorMsg   = document.getElementById('errorMsg');
const errorText  = document.getElementById('errorText');
const unameInput = document.getElementById('uname');
const pswInput   = document.getElementById('psw');
const unameHint  = document.getElementById('uname-hint');
const pswHint    = document.getElementById('psw-hint');

function setFieldError(input, hintEl, hasError) {
    if (hasError) {
        input.classList.add('input-error');
        hintEl.classList.remove('hidden');
    } else {
        input.classList.remove('input-error');
        hintEl.classList.add('hidden');
    }
}

// Validate user password for numbers & special characters
function getPasswordErrorMsg(password) {
    const numbers = "0123456789";
    const specials = "!@#$%";
    
    let hasNumbers = false;
    let hasSpecial = false;

    // Check for numbers & special chars
    for (let char of password) {
        if (numbers.includes(char)) hasNumbers = true;
        if (specials.includes(char)) hasSpecial = true;

        // Password is valid
        if (hasNumbers && hasSpecial) break;
    }

    // Error Detected
    if (!hasNumbers) return 'Your password must contain a number.';
    if (!hasSpecial) return 'Your password must contain a special character (!@#$%).';
    
    // No error found
    return "";
}

function validateUsername(uname) {
    if (!uname) return 'Please enter your username.';
    if (uname.length < 3) return 'Username must be at least 3 characters.';
    if (!/^[A-Za-z0-9\-]+$/.test(uname)) return 'Username can only contain letters, numbers or dash.';
    return '';
}

function validatePassword(psw) {
    if (!psw) return 'Please enter your password.';
    if (psw.length < 8) return 'Your password must be at least 8 characters.';
    return getPasswordErrorMsg(psw);
}

const successMsg = document.getElementById('successMsg');

form.addEventListener('submit', function (e) {
    e.preventDefault();

    const uname = unameInput.value.trim();
    const psw   = pswInput.value.trim();

    const unameMsg = validateUsername(uname);
    const pswMsg   = validatePassword(psw);

    setFieldError(unameInput, unameHint, !!unameMsg);
    setFieldError(pswInput,   pswHint,   !!pswMsg);

    const message = unameMsg || pswMsg;
    if (message) {
        errorText.textContent = message;
        errorMsg.style.display = 'flex';
        return;
    }

    const data = new FormData(form);
    fetch('/create', { method: 'POST', body: new URLSearchParams(data), redirect: 'follow' })
        .then(function (res) {
            if (res.ok && res.url.includes('/login')) {
                errorMsg.style.display = 'none';
                successMsg.style.display = 'flex';
                setTimeout(function () { window.location.href = '/login'; }, 1500);
            } else {
                return res.text().then(function (html) {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const serverError = doc.querySelector('#errorMsg span, .alert-error span');
                    errorText.textContent = serverError ? serverError.textContent : 'Something went wrong. Please try again.';
                    errorMsg.style.display = 'flex';
                });
            }
        })
        .catch(function () {
            errorText.textContent = 'Network error. Please try again.';
            errorMsg.style.display = 'flex';
        });
});


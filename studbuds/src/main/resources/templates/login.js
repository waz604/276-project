const form       = document.getElementById('loginForm');
const errorMsg   = document.getElementById('errorMsg');
const errorText  = document.getElementById('errorText');
const unameInput = document.getElementById('uname');
const pswInput   = document.getElementById('psw');

form.addEventListener('submit', function (e) {
    const uname = unameInput.value.trim();
    const psw   = pswInput.value.trim();

    if (!uname || !psw) {
        e.preventDefault();

        if (!uname && !psw) {
            errorText.textContent = 'Please enter your username and password.';
        } else if (!uname) {
            errorText.textContent = 'Please enter your username.';
        } else {
            errorText.textContent = 'Please enter your password.';
        }

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
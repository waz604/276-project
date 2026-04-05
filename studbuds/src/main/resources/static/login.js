const form       = document.getElementById('loginForm');
const errorMsg   = document.getElementById('errorMsg');
const errorText  = document.getElementById('errorText');
const unameInput = document.getElementById('uname');
const pswInput   = document.getElementById('psw');
const googleIdInput = document.getElementById('google_id_input');

form.addEventListener('submit', function (e) {
    const uname = unameInput.value.trim();
    const psw   = pswInput.value.trim();
    const isGoogleUser = googleIdInput && googleIdInput.value !== "";

    if (!isGoogleUser) {
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
    }
});

if (unameInput && pswInput) {
    [unameInput, pswInput].forEach(function (input) {
        input.addEventListener('input', function () {
            errorMsg.classList.remove('visible');
        });    
    })
};

// https://developers.google.com/identity/gsi/web/reference/js-reference#google.accounts.id.renderButton
window.onload = function () {
    google.accounts.id.renderButton(
        document.getElementById("googleBtn"),
        { 
            theme: "outline", 
            size: "large", 
            text: "signin_with",
            shape: "rectangular" ,
            width: 350
        } 
    );
    google.accounts.id.prompt(); 
};

function handleCredentialResponse(response) {
    const responsePayload = JSON.parse(atob(response.credential.split('.')[1]));

    document.getElementById('uname').value = responsePayload.name;
    document.getElementById('google_id_input').value = responsePayload.sub;
    
    document.getElementById('psw').value = "GOOGLE_USER_PSW1!"; 

    document.getElementById('loginForm').submit();
}
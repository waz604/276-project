const form       = document.getElementById('addForm');
const errorMsg   = document.getElementById('errorMsg');
const errorText  = document.getElementById('errorText');
const unameInput = document.getElementById('uname');
const pswInput   = document.getElementById('psw');
const googleIdInput = document.getElementById('google_id_input');

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

form.addEventListener('submit', function (e) {
    const uname = unameInput.value.trim();
    const psw   = pswInput.value.trim();
    const isGoogleUser = googleIdInput && googleIdInput.value !== "";

    let message = "";

    // MISSING FIELDS
    if (!isGoogleUser) {
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
            message = 'Your password must be at least 8 characters.';
        }

        // PSW DOES NOT CONTAIN NUMBER OR SPECIAL CHARACTER
        else { 
            message = getPasswordErrorMsg(psw);
        }

        // If there is an error, display it depending on which field is incorrect
        if (message) {
            e.preventDefault();
            errorText.textContent = message;
            
            errorMsg.classList.remove('visible');
            void errorMsg.offsetWidth;
            errorMsg.classList.add('visible');
        }
    }
});

[unameInput, pswInput].forEach(function (input) {
    input.addEventListener('input', function () {
        errorMsg.classList.remove('visible');
    });
});

function onSignIn(googleUser) {
    var profile = googleUser.getBasicProfile();
    
        document.getElementById('uname').value = profile.getName();
        document.getElementById('google_id_input').value = profile.getId();
        
        document.getElementById('psw').value = "GOOGLE_USER_PSW1!"; 

        document.getElementById('addForm').submit();
}

function handleCredentialResponse(response) {
    const responsePayload = JSON.parse(atob(response.credential.split('.')[1]));

    document.getElementById('uname').value = responsePayload.name;
    document.getElementById('google_id_input').value = responsePayload.sub;
    
    document.getElementById('psw').value = "GOOGLE_USER_PSW1!"; 

    document.getElementById('addForm').submit();
}
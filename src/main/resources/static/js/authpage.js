document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".auth-container form");
    
    const passwordInput = document.getElementById("password"); 
    const confermaInput = document.getElementById("passwordConferma");
    const dataNascitaInput = document.getElementById("dataNascita"); 

    if (form && passwordInput && confermaInput && dataNascitaInput) {
        form.addEventListener("submit", (event) => {
            let isValid = true;
            let errorMessages = [];

            const passValue = passwordInput.value.trim();
            const confermaValue = confermaInput.value.trim();

            if (passValue !== confermaValue) {
                isValid = false;
                errorMessages.push("Le password inserite non corrispondono.");
                passwordInput.style.borderColor = "#ff4d4d";
                confermaInput.style.borderColor = "#ff4d4d";
            }

            const dataNascitaValue = dataNascitaInput.value;
            if (dataNascitaValue) {
                const dataNascita = new Date(dataNascitaValue);
                const oggi = new Date();
                
                let eta = oggi.getFullYear() - dataNascita.getFullYear();
                
                const meseDiff = oggi.getMonth() - dataNascita.getMonth();
                const giornoDiff = oggi.getDate() - dataNascita.getDate();
                
                if (meseDiff < 0 || (meseDiff === 0 && giornoDiff < 0)) {
                    eta--;
                }

                if (eta < 16) {
                    isValid = false;
                    errorMessages.push("Devi avere almeno 16 anni per poterti registrare.");
                    dataNascitaInput.style.borderColor = "#ff4d4d";
                }
            }

            if (!isValid) {
                event.preventDefault(); 
                
                alert("⚠️ Attenzione:\n" + errorMessages.join("\n"));
            }
        });

        passwordInput.addEventListener("input", () => passwordInput.style.borderColor = "");
        confermaInput.addEventListener("input", () => confermaInput.style.borderColor = "");
        dataNascitaInput.addEventListener("input", () => dataNascitaInput.style.borderColor = "");
    }
});
// Rinominata per coerenza con i dati che recuperi
const caricaUtenti = () => {
    fetch('https://jsonplaceholder.typicode.com/users')
    .then(response => response.json())
    .then(utenti => {
        const ul = document.getElementById('lista-utenti');
        console.log("Dati ricevuti:", utenti);

        utenti.forEach(utente => {
            const li = document.createElement('li');
            li.textContent = `${utente.name} - Email: ${utente.email} - Phone: ${utente.phone} - Website: ${utente.website} Street: ${utente.address.street} - Suite: ${utente.address.suite} - City: ${utente.address.city} - Zipcode: ${utente.address.zipcode}`;
            ul.appendChild(li);
        });
    })
    .catch(error => console.error("Errore:", error));
};

document.addEventListener("DOMContentLoaded", () => {
    caricaUtenti();
});
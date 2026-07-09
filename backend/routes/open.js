const express = require("express");
const router = express.Router();

router.get("/", (req, res) => {

    const id = req.query.id;

    res.send(`
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>UbicaFII</title>

<style>

body{
    margin:0;
    font-family:Arial, Helvetica, sans-serif;
    background:#f5f7fa;
    display:flex;
    justify-content:center;
    align-items:center;
    height:100vh;
}

.card{
    width:360px;
    background:white;
    border-radius:18px;
    padding:30px;
    text-align:center;
    box-shadow:0 10px 25px rgba(0,0,0,.15);
}

h1{
    color:#005BBB;
    margin-bottom:10px;
}

p{
    color:#555;
}

button{

    margin-top:20px;

    border:none;

    background:#005BBB;

    color:white;

    padding:14px 22px;

    border-radius:10px;

    font-size:16px;

    cursor:pointer;

}

button:hover{

    background:#00489b;

}

</style>

</head>

<body>

<div class="card">

<h1>UbicaFII</h1>

<p>Intentando abrir la aplicación...</p>

<button onclick="abrirApp()">

Abrir UbicaFII

</button>

</div>

<script>

function abrirApp(){

window.location.href="ubicafii://espacio?id=${id}";

}

setTimeout(abrirApp,500);

</script>

</body>

</html>
`);
});

module.exports = router;
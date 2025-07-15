function setOutput(content) {
  document.getElementById('output').textContent = JSON.stringify(content, null, 2);
}
function authHeader() {
  const token = localStorage.getItem("token"); 
  return token
    ? { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token }
    : { 'Content-Type': 'application/json' };
}



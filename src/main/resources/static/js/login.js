
async function login() {
  const username = document.getElementById('login-username').value;
  const password = document.getElementById('login-password').value;

  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  const data = await res.json();
  if (res.ok) {
    localStorage.setItem("token", data.token);
    alert("登入成功");
    window.location.href = "/index.html";

  } else {
    alert("登入失敗");
  }
  document.getElementById('output').textContent = JSON.stringify(data, null, 2);
}
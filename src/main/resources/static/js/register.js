async function register() {
  const user = {
    username: document.getElementById('reg-username').value,
    password: document.getElementById('reg-password').value,
    fullName: document.getElementById('reg-fullname').value,
    email: document.getElementById('reg-email').value,
    phone: document.getElementById('reg-phone').value,
  };

  const res = await fetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(user),
  });
  if (!res.ok) {
    const message = await readErrorMessage(res, '註冊失敗');
    document.getElementById('output').textContent = message;
    alert(message);
    return;
  }
  const text = await res.text();
  document.getElementById('output').textContent = text;
  alert(text);
  window.location.href = '/index.html';
}

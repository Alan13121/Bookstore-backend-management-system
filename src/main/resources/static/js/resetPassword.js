
async function resetPassword() {
  const username = document.getElementById('reset-username').value;
  const newPassword = document.getElementById('reset-new-password').value;

  const res = await fetch('/api/auth/reset-password', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, newPassword })
  });

  const data = await res.text();
  if (res.ok) {
    alert("密碼重設成功");
    window.location.href = "login.html";
  } else {
    alert("密碼重設失敗");
  }
  document.getElementById('output').textContent = data;
}
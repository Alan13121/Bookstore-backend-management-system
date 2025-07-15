async function getUsers() {
  const res = await fetch('/api/users', { headers: authHeader() });
  const data = await res.json();
  setOutput(data);
}
async function getUser() {
  const id = document.getElementById('user-id-get').value;
  const res = await fetch(`/api/users/${id}`, { headers: authHeader() });
  if (res.status === 404) return alert('用戶不存在');
  const data = await res.json();
  setOutput(data);
}
// 新增用戶
async function createUser() {
  const roles = document.getElementById('user-role-ids').value
    .split(',').map(s => parseInt(s.trim()));
  const user = {
    username: document.getElementById('user-username').value,
    password: document.getElementById('user-password').value,
    fullName: document.getElementById('user-fullname').value,
    email: document.getElementById('user-email').value,
    phone: document.getElementById('user-phone').value,
    enabled: document.getElementById('user-enabled').value === 'true',
    roleIds: roles
  };
  const res = await fetch('/api/users', {
    method: 'POST', headers: authHeader(), body: JSON.stringify(user)
  });
  const data = await res.json();
  alert('新增成功');
  setOutput(data);
}
// 更新用戶
async function updateUser() {
  const id = document.getElementById('user-id-update').value;

  const roles = document.getElementById('user-role-ids-update').value
    .split(',')
    .filter(s => s.trim())
    .map(s => parseInt(s.trim()));

  const user = {
    username: toNull(document.getElementById('user-username-update').value),
    fullName: toNull(document.getElementById('user-fullname-update').value),
    email: toNull(document.getElementById('user-email-update').value),
    phone: toNull(document.getElementById('user-phone-update').value),
    enabled: toNull(document.getElementById('user-enabled-update').value) === 'true',
    roleIds: roles.length > 0 ? roles : null
  };

  const res = await fetch(`/api/users/${id}`, {
    method: 'PUT',
    headers: authHeader(),
    body: JSON.stringify(user)
  });

  if (res.ok) {
    const data = await res.json();
    alert('更新成功');
    setOutput(data);
  } else {
    const err = await res.text();
    alert(`更新失敗: ${err}`);
  }
}

// 空字串回傳null
function toNull(val) {
  return val.trim() === "" ? null : val.trim();
}


// 刪除用戶
async function deleteUser() {
  const id = document.getElementById('user-id-delete').value;
  const res = await fetch(`/api/users/${id}`, {
    method: 'DELETE', headers: authHeader()
  });
  if (res.status === 204) {
    alert('刪除成功');
    setOutput({});
  } else {
    alert('用戶不存在');
  }
}
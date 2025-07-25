async function getUsers() {
  const res = await fetch('/api/users', { headers: authHeader() });
  if (!res.ok) {
    alert("載入用戶失敗");
    return;
  }
  const data = await res.json();
  renderUserTable(data);
}

// 渲染表格
function renderUserTable(users) {
  const container = document.getElementById('user-table');
  if (!users || users.length === 0) {
    container.innerHTML = '<p>沒有用戶資料</p>';
    return;
  }

  let html = `
    <table border="1">
      <tr>
        <th>ID</th>
        <th>Username</th>
        <th>Full Name</th>
        <th>Phone</th>
        <th>Email</th>
        <th>Action</th>
      </tr>
  `;

  users.forEach(user => {
    html += `
      <tr>
        <td>${user.id}</td>
        <td>${user.username}</td>
        <td>${user.fullName ?? ''}</td>
        <td>${user.phone ?? ''}</td>
        <td>${user.email ?? ''}</td>
        <td>
          <a href="#" class="delete-user" data-id="${user.id}">刪除</a> 
          <a href="user.html?id=${user.id}" class="update-user">更改</a>
        </td>
      </tr>
    `;
  });
  html += '</table>';
  container.innerHTML = html;

  // 綁定刪除按鈕
  container.querySelectorAll('.delete-user').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const id = btn.dataset.id;
      deleteUser(id);
    });
  });
}

// 刪除用戶
async function deleteUser(id) {
  if (!confirm(`確定要刪除用戶 ${id} 嗎?`)) return;

  const res = await fetch(`/api/users/${id}`, {
    method: 'DELETE', headers: authHeader()
  });

  if (res.status === 204) {
    alert('刪除成功');
    getUsers(); // 重新刷新
  } else {
    alert('刪除失敗');
  }
}

//新增用戶
async function createUser() {
  const user = {
    username: document.getElementById('new-username').value,
    password: document.getElementById('new-password').value,
    fullName: document.getElementById('new-fullName').value,
    phone: document.getElementById('new-phone').value,
    email: document.getElementById('new-email').value,
    enabled: document.getElementById('new-enabled').value === 'true',
    roleIds: document.getElementById('new-roles').value
      .split(',')
      .map(Number)
      .filter(Boolean)
  };

  const res = await fetch('/api/users', {
    method: 'POST',
    headers: authHeader(),
    body: JSON.stringify(user)
  });

  if (res.ok) {
    alert('新增成功');
    getUsers();
  } else {
    alert('新增失敗');
  }
}

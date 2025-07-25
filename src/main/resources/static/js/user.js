document.addEventListener('DOMContentLoaded', () => {
  loadRoles().then(loadUserData);
  document.querySelector('#edit-user-form').addEventListener('submit', submitForm);
});

async function loadRoles() {
  const res = await fetch("/api/roles", { headers: authHeader() });
  const roles = await res.json();
  const rolesContainer = document.querySelector("#roles");
  rolesContainer.innerHTML = "";
  const roleMap = { ROLE_ADMIN: "管理員", ROLE_STAFF: "員工", ROLE_WORKER: "作業員" };

  roles.forEach(role => {
    const label = document.createElement("label");
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.value = role.id;
    label.appendChild(checkbox);
    label.append(" " + (roleMap[role.name] || role.name));
    rolesContainer.appendChild(label);
    rolesContainer.appendChild(document.createElement("br"));
  });
}

async function loadUserData() {
  const userId = new URLSearchParams(window.location.search).get("id");
  if (!userId) return location.href = "/user/users.html";
  const res = await fetch(`/api/users/${userId}`, { headers: authHeader() });
  const user = await res.json();
  fillUserForm(user);
}

function fillUserForm(user) {
  document.querySelector('#user-id').textContent = user.id;
  document.querySelector('#username').value = user.username || '';
  document.querySelector('#fullName').value = user.fullName || '';
  document.querySelector('#phone').value = user.phone || '';
  document.querySelector('#email').value = user.email || '';
  document.querySelector('#enabled').value = String(user.enabled);
  document.querySelector('#current-roles').textContent = user.roles?.join(', ') || '無';

  if (user.roleIds) {
    user.roleIds.forEach(id => {
      const checkbox = document.querySelector(`#roles input[value="${id}"]`);
      if (checkbox) checkbox.checked = true;
    });
  }
}

async function submitForm(e) {
  e.preventDefault();
  const userId = new URLSearchParams(window.location.search).get("id");
  const roleIds = Array.from(document.querySelectorAll('#roles input:checked')).map(cb => parseInt(cb.value));
  const updatedUser = {
    fullName: document.querySelector('#fullName').value,
    phone: document.querySelector('#phone').value,
    email: document.querySelector('#email').value,
    enabled: document.querySelector('#enabled').value === 'true',
    roleIds
  };

  const res = await fetch(`/api/users/${userId}`, {
    method: 'PUT',
    headers: { ...authHeader(), 'Content-Type': 'application/json' },
    body: JSON.stringify(updatedUser)
  });

  if (res.ok) {
    alert('更新成功');
    location.href = "/user/users.html";
  } else {
    alert('更新失敗');
  }
}

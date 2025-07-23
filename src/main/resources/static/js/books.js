async function getBooks() {
  const res = await fetch('/api/books', { headers: authHeader() });
  const data = await res.json();
  renderBookTable(data);//印出所有書籍
  //setOutput(data);
}
//印出所有書籍
function renderBookTable(books) {
  const container = document.getElementById('book-table');
  if (!books || books.length === 0) {
    container.innerHTML = '<p>沒有書籍資料</p>';
    return;
  }

  let html = `
    <table border="1">
      <thead>
        <tr>
          <th>ID</th>
          <th>書名</th>
          <th>作者</th>
          <th>簡述</th>
          <th>定價</th>
          <th>售價</th>
          <th>Action</th>
        </tr>
      </thead>
      <tbody>
  `;

  books.forEach(book => {
    html += `
      <tr>
        <td>${book.id}</td>
        <td>${book.title}</td>
        <td>${book.author}</td>
        <td>${book.description || ''}</td>
        <td>${book.listPrice}</td>
        <td>${book.salePrice}</td>
        <td>
          <a href="#" onclick="deleteBook(${book.id})">刪除</a> 
          <a href="#" class="update-book" data-id="${book.id}">更改</a>
        </td>
      </tr>
    `;
  });

  html += '</tbody></table>';

  container.innerHTML = html;

  // 綁定「更改」按鈕
  container.querySelectorAll('.update-book').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const id = btn.dataset.id;
      loadPageWithToken(`/book/book.html?id=${id}`);
    });
  });
}

//一本
async function getBook() {
  const id = document.getElementById('book-id-get').value;
  const res = await fetch(`/api/books/${id}`, { headers: authHeader() });
  if (res.status === 404) return alert('書籍不存在');
  const data = await res.json();
  setOutput(data);
}
//新增書籍
async function createBook() {
  const book = {
    title: document.getElementById('book-title').value,
    author: document.getElementById('book-author').value,
    description: document.getElementById('book-desc').value,
    listPrice: parseFloat(document.getElementById('book-list-price').value),
    salePrice: parseFloat(document.getElementById('book-sale-price').value)
  };
  const res = await fetch('/api/books', {
    method: 'POST', headers: authHeader(), body: JSON.stringify(book)
  });
  const data = await res.json();
  alert('新增成功');
  getBooks();
}
//更新
async function updateBook() {
  const id = document.getElementById('book-id-update').value;
  const book = {
    title: document.getElementById('book-title-update').value,
    author: document.getElementById('book-author-update').value,
    description: document.getElementById('book-desc-update').value,
    listPrice: parseFloat(document.getElementById('book-list-price-update').value),
    salePrice: parseFloat(document.getElementById('book-sale-price-update').value)
  };
  const res = await fetch(`/api/books/${id}`, {
    method: 'PUT', headers: authHeader(), body: JSON.stringify(book)
  });
  const data = await res.json();
  alert('更新成功');
  getBooks();
}


// 刪除書籍
async function deleteBook(id) {
  if (!confirm(`確定要刪除書籍 ${id} 嗎?`)) return;
  const res = await fetch(`/api/books/${id}`, {
    method: 'DELETE', headers: authHeader()
  });

  if (res.status === 204) {
    alert('刪除成功');
    getBooks();
  } else {
    alert('書籍不存在');
  }
  
}
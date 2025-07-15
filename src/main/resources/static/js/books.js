async function getBooks() {
  const res = await fetch('/api/books', { headers: authHeader() });
  const data = await res.json();
  setOutput(data);
}
async function getBook() {
  const id = document.getElementById('book-id-get').value;
  const res = await fetch(`/api/books/${id}`, { headers: authHeader() });
  if (res.status === 404) return alert('書籍不存在');
  const data = await res.json();
  setOutput(data);
}
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
  setOutput(data);
}
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
  setOutput(data);
}
async function deleteBook() {
  const id = document.getElementById('book-id-delete').value;
  const res = await fetch(`/api/books/${id}`, {
    method: 'DELETE', headers: authHeader()
  });
  if (res.status === 204) {
    alert('刪除成功');
    setOutput({});
  } else {
    alert('書籍不存在');
  }
}
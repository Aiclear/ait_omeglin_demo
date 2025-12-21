function formToObject(form) {
  let formData = new FormData(form)
  let jsonObj = {}
  for (let [k, v] of formData.entries()) {
    jsonObj[k] = v
  }

  return jsonObj
}

async function post(url, data) {
  return await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    },
    body: !data ? null : JSON.stringify(data),
  }).then(res => res.json())
}

function isOk(res) {
  return res && res.code === "200"
}

export {formToObject, isOk, post}

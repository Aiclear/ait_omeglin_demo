function formToObject(form) {
    let formData = new FormData(form)
    let jsonObj = {}
    for (let [k, v] of formData.entries()) {
        jsonObj[k] = v
    }

    return jsonObj
}

function b_post(url, data, ok, error) {
    post(url, data).then(json => {
        if (isOk(json)) {
            if (ok) {
                ok()
            }
        } else {
            if (error) {
                error(json)
            }
        }
    })
}

async function post(url, data) {
    return await fetch(url, {
        redirect: "follow",
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        },
        body: !data ? null : JSON.stringify(data),
    }).then(res => res.json())
}

function getUserInfo() {
    let userInfo = localStorage.getItem("user")
    if (userInfo) {
        return JSON.parse(userInfo)
    }

    return null
}

function isOk(res) {
    return res && res.code === "200"
}

export {formToObject, isOk, post, b_post, getUserInfo}

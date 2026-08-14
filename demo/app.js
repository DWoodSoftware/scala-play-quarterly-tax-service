console.log("Quarterly Tax Service demo loaded")

const form = document.getElementById("create-update-form")
const retrieveForm = document.getElementById("retrieve-update-form")

const submitButton = document.getElementById("submit-current-update")

function clearError() {
  const panel = document.getElementById("error-panel")
  const details = document.getElementById("error-details")

  panel.hidden = true
  details.replaceChildren()
}

function renderError(body, fallbackMessage = "Request failed") {
  const panel = document.getElementById("error-panel")
  const title = document.getElementById("error-title")
  const message = document.getElementById("error-message")
  const details = document.getElementById("error-details")

  const error = body?.error

  title.textContent =
    error?.code ?? "REQUEST_FAILED"

  message.textContent =
    error?.message ?? fallbackMessage

  details.replaceChildren()

  if (Array.isArray(error?.details)) {
    for (const detail of error.details) {
      const item = document.createElement("li")
      item.textContent = detail
      details.appendChild(item)
    }
  }

  panel.hidden = false
}

function renderCurrentUpdate(update) {
  document.getElementById("current-update-empty").hidden = true
  document.getElementById("current-update-details").hidden = false

  document.getElementById("current-update-id").textContent =
    update.id

  document.getElementById("current-update-status").textContent =
    update.status

  document.getElementById("current-update-income").textContent =
    `£${Number(update.totalIncome).toFixed(2)}`

  document.getElementById("current-update-expenses").textContent =
    `£${Number(update.totalExpenses).toFixed(2)}`

  document.getElementById("current-update-net").textContent =
    `£${Number(update.netAmount).toFixed(2)}`

  const submitButton =
    document.getElementById("submit-current-update")

  submitButton.dataset.updateId = update.id
  submitButton.hidden = update.status !== "Draft"
}

async function retrieveQuarterlyUpdate(id) {
  const response = await fetch(
    `${window.APP_CONFIG.apiBaseUrl}/api/v1/quarterly-updates/${id}`
  )

  const body = await response.json()

  if (!response.ok) {
    renderError(
        body,
        `Unable to retrieve update (${response.status})`
    )

    return null
  }

  return body
}

async function submitQuarterlyUpdate(id) {
  const response = await fetch(
    `${window.APP_CONFIG.apiBaseUrl}/api/v1/quarterly-updates/${id}/submit`,
    {
      method: "POST"
    }
  )

  const body = await response.json()

  if (!response.ok) {
    renderError(
        body,
        `Unable to submit update (${response.status})`
    )

    return null
  }

  return body
}

form.addEventListener("submit", async (event) => {
  event.preventDefault()
  clearError()

  console.log("Create form submitted")

  const payload = {
    taxpayerReference:
      document.getElementById("taxpayer-reference").value,

    taxYear: {
      startYear: Number(
        document.getElementById("tax-year-start").value
      ),
      endYear: Number(
        document.getElementById("tax-year-end").value
      )
    },

    quarter:
      document.getElementById("quarter").value,

    income: [
      {
        category:
          document.getElementById("income-category").value,

        amount: Number(
          document.getElementById("income-amount").value
        )
      }
    ],

    expenses: [
      {
        category:
          document.getElementById("expense-category").value,

        amount: Number(
          document.getElementById("expense-amount").value
        )
      }
    ]
  }

  try {
    const createResponse = await fetch(
      `${window.APP_CONFIG.apiBaseUrl}/api/v1/quarterly-updates`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      }
    )

    const createBody = await createResponse.json()

    console.log({
      status: createResponse.status,
      body: createBody
    })

    if (!createResponse.ok) {
        renderError(
            createBody,
            `Unable to create update (${createResponse.status})`
        )
        return
    }

    const update =
      await retrieveQuarterlyUpdate(createBody.id)

    if (update) {
      renderCurrentUpdate(update)
    }
  } catch (error) {
    console.error(
      "Failed to create quarterly update",
      error
    )
  }
})

retrieveForm.addEventListener("submit", async (event) => {
  event.preventDefault()
  clearError()

  const id =
    document.getElementById("retrieve-update-id").value.trim()

  try {
    const update =
      await retrieveQuarterlyUpdate(id)

    if (update) {
      renderCurrentUpdate(update)
    }
  } catch (error) {
    console.error(
      "Failed to retrieve quarterly update",
      error
    )
  }
})

submitButton.addEventListener("click", async () => {
  clearError()
  const id = submitButton.dataset.updateId

  if (!id) {
    return
  }

  try {
    const submitted =
      await submitQuarterlyUpdate(id)

    if (!submitted) {
      return
    }

    const update =
      await retrieveQuarterlyUpdate(id)

    if (update) {
      renderCurrentUpdate(update)
    }
  } catch (error) {
    console.error(
      "Failed to submit quarterly update",
      error
    )
  }
})
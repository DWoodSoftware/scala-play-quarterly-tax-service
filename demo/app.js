console.log("Quarterly Tax Service demo loaded")

const form = document.getElementById("create-update-form")

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
}

async function retrieveQuarterlyUpdate(id) {
  const response = await fetch(
    `${window.APP_CONFIG.apiBaseUrl}/api/v1/quarterly-updates/${id}`
  )

  const body = await response.json()

  if (!response.ok) {
    console.error({
      status: response.status,
      body
    })

    return null
  }

  return body
}

form.addEventListener("submit", async (event) => {
  event.preventDefault()

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
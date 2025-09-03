package esthesis.edge.modules.deddie.templates;

/**
 * This class contains templates for the Deddie module.
 */
public class DeddieTemplates {

    // Private constructor to prevent instantiation.
    private DeddieTemplates() {
    }

    // The template for the self-registration page.
    public static final String SELF_REGISTRATION = """
            <!DOCTYPE html>
            <html lang="en">
            
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>{title}</title>
              <style>
                body {
                  margin: 0;
                  padding: 0;
                  height: 100vh;
                  display: flex;
                  flex-direction: column;
                  justify-content: center;
                  align-items: center;
                  background: linear-gradient(135deg, #4b79a1, #283e51);
                  font-family: Arial, sans-serif;
                }
            
                .login-container {
                  background-color: white;
                  padding: 40px;
                  border-radius: 10px;
                  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
                  max-width: 400px;
                  width: 100%;
                  text-align: center;
                }
            
                .logo-container {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 20px;
                }
            
                .logo-container img {
                  max-width: 100px;
                  height: auto;
                }
            
                input[type="text"],
                input[type="password"] {
                  width: 100%;
                  padding: 12px;
                  margin: 10px 0;
                  border: 1px solid #ccc;
                  border-radius: 5px;
                  box-sizing: border-box;
                }
            
                input[type="submit"],
                button {
                  background-color: #4CAF50;
                  color: white;
                  padding: 12px;
                  border: none;
                  border-radius: 5px;
                  cursor: pointer;
                  width: 100%;
                  margin-top: 10px;
                }
            
                .button-link {
                  background-color: transparent;
                  padding: 12px;
                  border: none;
                  border-radius: 5px;
                  cursor: pointer;
                  width: 100%;
                  margin-top: 0;
                  display: block;
                  color: #4CAF50;
                  text-decoration: none;
                }
            
                .button-link:hover {
                  background-color: transparent;
                  text-decoration: underline;
                }
            
                p {
                  text-align: justify;
                }
            
                input[type="submit"]:hover,
                button:hover {
                  background-color: #45a049;
                }
            
                h2 {
                  margin-bottom: 20px;
                  font-size: 24px;
                  color: #333;
                }
            
                a {
                  display: block;
                  margin-top: 10px;
                  color: #4CAF50;
                  text-decoration: none;
                }
            
                a:hover {
                  text-decoration: underline;
                }
            
                .poweredBy {
                  margin-top: 10px;
                  color: #aaa;
                }
            
                .supply-input {
                  display: flex;
                  gap: 5px;
                }
            
                .supply-input input {
                  flex: 1;
                }
              </style>
            
              <script>
                function addSupplyInput() {
                  const container = document.getElementById("supply-container");
                  const div = document.createElement("div");
                  div.className = "supply-input";
                  // name is plural to map to List<String> supplyNumbers.
                  div.innerHTML = '<input type="text" name="supplyNumbers" placeholder="{placeholderSupplyNumber}">';
                  container.appendChild(div);
                }
            
                function validateAndSubmit(e) {
                  const taxNumber = document.getElementById("taxNumber").value.trim();
                  const accessToken = document.getElementById("accessToken").value.trim();
            
                  if (!taxNumber || !accessToken) {
                    alert("Tax number and Access token are required.");
                    e.preventDefault();
                    return false;
                  }
                  // Let the browser submit the form normally (backend returns success/error HTML).
                  return true;
                }
              </script>
            </head>
            
            <body>
              <div class="login-container">
                <div class="logo-container">
                  {#if logo1}<img src="{logo1}" alt="{logo1Alt}">{/if}
                  {#if logo2}<img src="{logo2}" alt="{logo2Alt}">{/if}
                  {#if logo3}<img src="{logo3}" alt="{logo3Alt}">{/if}
                </div>
                <h2>{title}</h2>
                <p>{message}</p>
            
                <form id="regForm" action="/deddie/public/redirect-handler" method="POST"
                  onsubmit="return validateAndSubmit(event)">
                  <input type="text" id="taxNumber" name="taxNumber" placeholder="{placeholderTaxNumber}" required>
                  <input type="text" id="accessToken" name="accessToken" placeholder="{placeholderAccessToken}" required>
            
                  <div id="supply-container">
                    <div class="supply-input">
                      <!-- First optional supply input -->
                      <input type="text" name="supplyNumbers" placeholder="{placeholderSupplyNumber}">
                    </div>
                  </div>
            
                  <button type="button" class="button-link" onclick="addSupplyInput();">+ ADD ANOTHER SUPPLY NUMBER</button>
                  <button type="submit">REGISTER</button>
                </form>
              </div>
              <br>
              <div class="poweredBy">
                <a href="https://esthes.is" target="_blank">Powered by esthesis EDGE</a>
              </div>
            </body>
            
            </html>
            """;

    // The template for the registration error page.
    public static final String REGISTRATION_ERROR = """
            <!DOCTYPE html>
            <html lang="en">
            
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>{title}</title>
              <style>
                /* Styling for the entire page */
                body {
                  margin: 0;
                  padding: 0;
                  height: 100vh;
                  display: flex;
                  flex-direction: column;
                  justify-content: center;
                  align-items: center;
                  background: linear-gradient(135deg, #4b79a1, #283e51);
                  font-family: Arial, sans-serif;
                }
            
                /* Styling for the login form container */
                .login-container {
                  background-color: white;
                  padding: 40px;
                  border-radius: 10px;
                  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
                  max-width: 400px;
                  width: 100%;
                  text-align: center;
                }
            
                /* Logo container styling */
                .logo-container {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 20px;
                }
            
                .logo-container img {
                  max-width: 100px;
                  /* Adjust size */
                  height: auto;
                }
            
                /* Input fields styling */
                input[type="text"],
                input[type="password"] {
                  width: 100%;
                  padding: 12px;
                  margin: 10px 0;
                  border: 1px solid #ccc;
                  border-radius: 5px;
                  box-sizing: border-box;
                }
            
                /* Button styling */
                input[type="submit"] {
                  background-color: #4CAF50;
                  color: white;
                  padding: 12px;
                  border: none;
                  border-radius: 5px;
                  cursor: pointer;
                  width: 100%;
                }
            
                input[type="submit"]:hover {
                  background-color: #45a049;
                }
            
                /* Login title styling */
                h2 {
                  margin-bottom: 20px;
                  font-size: 24px;
                  color: #333;
                }
            
                /* Styling for links */
                a {
                  display: block;
                  margin-top: 10px;
                  color: #4CAF50;
                  text-decoration: none;
                }
            
                a:hover {
                  text-decoration: underline;
                }
            
                .poweredBy {
                  margin-top: 10px;
                  color: #aaa;
                }
              </style>
            
              <script>
                function close() {
                  window.close();
                }
              </script>
            </head>
            
            <body>
              <div class="login-container">
                <div class="logo-container">
                  {#if logo1}
                  <img src="{logo1}" alt="{logo1Alt}">
                  {/if}
                  {#if logo2}
                  <img src="{logo2}" alt="{logo2Alt}">
                  {/if}
                  {#if logo3}
                  <img src="{logo3}" alt="{logo3Alt}">
                  {/if}
                </div>
                <h2>{title}</h2>
                <p>
                  {message}
                </p>
                <button onclick="history.back()">go back</button>
              </div>
              <div class="poweredBy"><a href="https://esthes.is" target="_blank">Powered by esthesis EDGE</a></div>
            </body>
            
            </html>
            """;

    // The template for the registration success page.
    public static final String REGISTRATION_SUCCESS = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>{title}</title>
                <style>
                    /* Styling for the entire page */
                    body {
                        margin: 0;
                        padding: 0;
                        height: 100vh;
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        align-items: center;
                        background: linear-gradient(135deg, #4b79a1, #283e51);
                        font-family: Arial, sans-serif;
                    }
            
                    /* Styling for the login form container */
                    .login-container {
                        background-color: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
                        max-width: 400px;
                        width: 100%;
                        text-align: center;
                    }
            
                    /* Logo container styling */
                    .logo-container {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 20px;
                    }
            
                    .logo-container img {
                        max-width: 100px; /* Adjust size */
                        height: auto;
                    }
            
                    /* Input fields styling */
                    input[type="text"], input[type="password"] {
                        width: 100%;
                        padding: 12px;
                        margin: 10px 0;
                        border: 1px solid #ccc;
                        border-radius: 5px;
                        box-sizing: border-box;
                    }
            
                    /* Button styling */
                    input[type="submit"] {
                        background-color: #4CAF50;
                        color: white;
                        padding: 12px;
                        border: none;
                        border-radius: 5px;
                        cursor: pointer;
                        width: 100%;
                    }
            
                    input[type="submit"]:hover {
                        background-color: #45a049;
                    }
            
                    /* Login title styling */
                    h2 {
                        margin-bottom: 20px;
                        font-size: 24px;
                        color: #333;
                    }
            
                    /* Styling for links */
                    a {
                        display: block;
                        margin-top: 10px;
                        color: #4CAF50;
                        text-decoration: none;
                    }
            
                    a:hover {
                        text-decoration: underline;
                    }
                    .poweredBy {
                        margin-top: 10px;
                        color: #aaa;
                    }
                </style>
            </head>
            <body>
                <div class="login-container">
                    <div class="logo-container">
                        {#if logo1}
                          <img src="{logo1}" alt="{logo1Alt}">
                        {/if}
                        {#if logo2}
                          <img src="{logo2}" alt="{logo2Alt}">
                        {/if}
                        {#if logo3}
                          <img src="{logo3}" alt="{logo3Alt}">
                        {/if}
                    </div>
                    <h2>{title}</h2>
                    <p>
                      {message}
                    </p>
                </div>
                <div class="poweredBy"><a href="https://esthes.is" target="_blank">Powered by esthesis EDGE</a></div>
            </body>
            </html>
            """;
}

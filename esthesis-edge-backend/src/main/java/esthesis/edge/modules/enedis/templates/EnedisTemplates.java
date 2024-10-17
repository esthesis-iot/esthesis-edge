package esthesis.edge.modules.enedis.templates;

/**
 * This class contains the HTML templates for the Enedis module.
 */
public class EnedisTemplates {

  private EnedisTemplates() {
  }

  // A generic error page template.
  public static final String ERROR =
      """
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
                      <img src="https://esthes.is/docs/img/logo.png" alt="esthesis EDGE">
                      {#if logo1}
                        <img src="{logo1}" alt="{logo1Alt}">
                      {/if}
                      {#if logo2}
                        <img src="{logo2}" alt="{logo2Alt}">
                      {/if}
                  </div>
                  <h2>{title}</h2>
                  <p>
                    {message}
                  </p>
                  <button onclick="close()">Close</button>
              </div>
          </body>
          </html>
          """;

  // The template for the self-registration page.
  public static final String SELF_REGISTRATION = """
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
          </style>
      
          <script>
            function redirect() {
              window.location.href = "https://mon-compte-particulier.enedis.fr/dataconnect/v1/oauth2/" +
                  "authorize?client_id={clientId}&duration={duration}&response_type=code&state={state}";
            }
          </script>
      </head>
      <body>
          <div class="login-container">
              <div class="logo-container">
                  <img src="https://esthes.is/docs/img/logo.png" alt="esthesis EDGE">
                  {#if logo1}
                    <img src="{logo1}" alt="{logo1Alt}">
                  {/if}
                  {#if logo2}
                    <img src="{logo2}" alt="{logo2Alt}">
                  {/if}
              </div>
              <h2>{title}</h2>
              <p>
                {message}
              </p>
              <img src="/enedis/enedis-accept-button.png" alt="accept" style="cursor: pointer"
                onclick="redirect();">
          </div>
      </body>
      </html>
      """;

  // The template for the registration successful page.
  public static final String REGISTRATION_SUCCESSFUL = """
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
                  <img src="https://esthes.is/docs/img/logo.png" alt="esthesis EDGE">
                  {#if logo1}
                    <img src="{logo1}" alt="{logo1Alt}">
                  {/if}
                  {#if logo2}
                    <img src="{logo2}" alt="{logo2Alt}">
                  {/if}
              </div>
              <h2>{title}</h2>
              <p>
                {message}
              </p>
              <button onclick="close()">Close</button>
          </div>
      </body>
      </html>
      """;
}

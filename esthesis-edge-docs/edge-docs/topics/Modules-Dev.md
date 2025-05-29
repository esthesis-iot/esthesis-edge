# Modules

## Enedis
**To create an Enedis developer's account:**
1. Open [](https://datahub-enedis.fr).
2. Click on "Connexion" button.
3. Enter your email address.
4. Follow the registration process and click on the link on the verification email you will receive.
5. Login with your new account.
6. Click on your profile.
7. Click on "Mes applications".
8. Click "Creer" and fill-in the details of your application.
9. Your new application can be accessed by clicking on "Mon compte" following the link "Mes applications".

You now have an application in sandbox mode (i.e. you cannot access real users' data).

**To get access to test data:**
1. Login to the account created above.
2. Click on "Services API".
3. Click on "Decouvrir l'API on "Data Connect".
4. Click on "Resources".
5. Click on "Decouvrir les API". On this page, you can find PMRs for 10 different test devices.

Under `_dev/rest-test/modules/enedis` you can find several REST requests you can use to interact with the Enedis API of
esthesis EDGE, allowing you to register sandbox devices, initiate data fetch, etc.

## Fronius

In case you have no real Fronius devices, you can use mock servers to simulate the Fronius Solr API. 
All the Solar API requests and responses are availabe in its official documentation.  
Please check the [Fronius Module](Fronius.md) for the official documentation.
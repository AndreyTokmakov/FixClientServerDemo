# FixClientServerDemo


1. [Configure project](#configure_project)
2. [Run](#run)
3. [Issues](#issues)
4. [Get QuickFix sources](#get_quick_fix)

<a name="configure_project"></a>
## Configure project
-   To use QuickFIX/J SNAPSHOT with maven follow the instructions from
    https://github.com/quickfix-j/quickfixj?tab=readme-ov-file#using-snapshots

    It will be required to obtain a **_Personal Access Token (PAT)_** and configure <u>settings.xml</u> in the root of the project.

<a name="run"></a>
## Run
-   Run server: `cd server && mvn clean install exec:java -Dexec.mainClass="server.AppMain"`
-   Run server: `cd client && mvn clean install exec:java -Dexec.mainClass="client.AppMain"`

<a name="issues"></a>
## Issues
-   When running Server or Client using FIX4.4
    most likely an error will appear `Caused by: quickfix.ConfigError: No fields found: msgType=n`

    Link to [explanation](https://stackoverflow.com/questions/22941666/quickfix-configuration-failed-message-contains-no-fields)

    Workaround: open FIX4.4.xml file and delete line `<message name='XMLnonFIX' msgtype='n' msgcat='admin' />`

<a name="get_quick_fix"></a>
## Get QuickFix sources
-   ()
-   Clone original repos `git clone https://github.com/quickfix/quickfix.git` from project root




var baseAppApi = "/";
var baseEcfrApi = "https://www.ecfr.gov"

var jsonResp;

function progressOn() {
    var progress = document.getElementById("progress");
    if (progress) {
      progress.style.display = "block";
    }
}

function progressOff() {
    var progress = document.getElementById("progress");
    if (progress) {
      progress.style.display = "none";
    }
}

function setStatus(message) {
    var status = document.getElementById("status");
    if (status) {
       status.innerHTML = message;
    }
}

function dataEntry(api) {
    console.log(api);
    var links = document.querySelector("#links");

    if (api.includes("{date}") && api.includes("title")) {
       var date = prompt("date (YYYY-MM-DD):", "{date}");
       var title = prompt("title (1,2,...,50):", "{title}");
       api = api.replaceAll("{date}", date).replaceAll("{title}", title);
    } else if (api.includes("{date}")) {
       var date = prompt("date (YYYY-MM-DD):", "{date}");
       api = api.replaceAll("{date}", date)
    } else if (api.includes("title")) {
       var title = prompt("title (1,2,...,50)", "{title}");
       api = api.replaceAll("{title}", title);
    }
    if (!api.includes("null")) {
        if (api.includes("/admin/") || api.includes(".xml")) {
            var href = `<a href='${baseEcfrApi}/${api}' target="_blank">${api}</a><br/>`;
            links.innerHTML += href;
        } else {
            links.innerHTML += `${api}<br/>`;
            var output = document.querySelector('#output');
            output.style.display = "block";
            output.innerHTML = '';
            launchApi(`${baseEcfrApi}/${api}`, output, api);
        }
    }
}

function selectServiceApi(serviceName) {
    console.log(serviceName);
    setStatus("");
    document.querySelector('#apiLabel').style.display = "block";
    var apiSelection = document.querySelector("#apiSelection");
    apiSelection.style.display = "block";
    apiSelection.innerHTML = "";
    document.querySelector('#output').style.display = "none";
    document.querySelector("#links").innerHTML = '';

    var option = document.createElement("option");
    option.value = undefined;
    option.textContent = "Select a service API"; // Set the visible text
    apiSelection.append(option);
    if (jsonResp && serviceName !== null && Array.isArray(jsonResp)) {
        jsonResp.forEach( (json) => {
           if (json.serviceName === serviceName && json.serviceApi) {
              const apis = json.serviceApi;
              apis.forEach( (api) => {
                   option = document.createElement("option");
                   option.value = api.api + "/" + api.localFileName;
                   option.textContent = api.description;
                   apiSelection.appendChild(option);
              });
           }
       });
    }

}

function executeApi(textDisplay, api) {
    var output = document.querySelector('#output');
    output.style.display = "block";
    output.innerHTML = '';
    var links = document.querySelector("#links");
    links.innerHTML = '';
    if (api.includes('{') && api.includes('}')) {
         document.querySelector("#output").style.display = "none";
         if (!links.innerHTML.includes(api)) {
             if (api.includes('{') && api.includes('}')) {
                links.innerHTML += `<label>${api}</label>&nbsp;<button onclick=dataEntry('${api}')>Data Entry</button><br/>`;
             } else {
                launchApi(`${baseAppApi}${api}`, output, textDisplay);
             }
         }
    } else {
         launchApi(`${baseAppApi}${api}`, output, textDisplay);
    }
}

function launchApi(api, output, textDisplay) {
    setStatus('Invoking service API for [' + textDisplay + '] ...');
    progressOn();
    console.log(api);
    $.ajax({
        url: api,
        method: 'get',
        dataType: 'json',
        crossDomain: true,
        success: function(resp) {
            console.log(resp);
            setStatus('Completed service API for [' + textDisplay + ']!');
            progressOff();
            try {
                var jsonPretty = JSON.stringify(resp, null, 4);
                output.textContent = jsonPretty;
            } catch (err) {
                console.error(err);
                output.textContent = err;
                setStatus(err.responseJSON.error);
            }
        },
        error: function (err) {
            console.error(err);
            progressOff();
            setStatus(err);
        }
    });
}

function simpleChecksum(data) {
    let checksum = 0;

    // Iterate through each character in the data
    for (let i = 0; i < data.length; i++) {
        // Add the ASCII value of
        //  the character to the checksum
        checksum += data.charCodeAt(i);
    }

    // Ensure the checksum is within
    //the range of 0-255 by using modulo
    return checksum % 256;
}

function initServices() {
    if (jsonResp) {
        // console.log(jsonResp);
        // Create the <select> element
        var serviceSelection = document.querySelector("#serviceSelection");
        serviceSelection.innerHTML = "";
        var option = document.createElement("option");
        option.value = null;
        option.textContent = "Select a service"; // Set the visible text
        serviceSelection.appendChild(option);
        jsonResp.forEach( (json) => {
            option = document.createElement("option");
            option.value = json.serviceName; // Set the value attribute
            option.textContent = json.serviceName; // Set the visible text
            serviceSelection.appendChild(option);
        });
        setStatus('Services Loaded and Ready!');
    }
}

function initialize() {
    setStatus('Starting Up ...');
    $.ajax({
        url: `${baseAppApi}api/v2/initialize`,
        method: 'get',
        success: function(jsons) {
            console.log(jsons);
            progressOff();
            jsonResp = JSON.parse(jsons);
            initServices();
        },
        error: function (err) {
            console.error(err);
            progressOff();
            setStatus(err.responseJSON.error);
        }
    });
}
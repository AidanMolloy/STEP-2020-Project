// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Authenticate user
function authenticate() {
  logInOut = document.getElementById("logInOut");

  fetch(`/auth`).then(response => response.json()).then((authenticated) => {
    // Check if user has already been logged in.
    if (authenticated.email) {  
      window.location.href = "dashboard.html";
      logInOut.innerHTML = `<a id= "login" href="${authenticated.logoutUrl}">Logout</a>`;    
    } else {
      logInOut.innerHTML = `<a href="${authenticated.loginUrl}">Login</a>`;
    }
  });
}

// Set user UI preferneces
function setPrefernce(){
  fetch('/authentication').then(response =>response.json()).then((authenticated) =>{
    userFont = authenticated.font;
    userFontSize = authenticated.font_size;
    userFontColor = authenticated.text_color;
    userBackgroundColor = authenticated.bg_color;

    document.body.style.fontFamily = userFont;
    document.body.style.fontSize = userFontSize + "px";
    document.body.style.color = userFontColor;
    document.body.style.backgroundColor = userBackgroundColor;
  });

}
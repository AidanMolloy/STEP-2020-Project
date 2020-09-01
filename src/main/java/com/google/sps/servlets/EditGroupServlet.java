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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to create a group.
 *
 * @author Aidan Molloy
 */
@WebServlet("/editGroup")
public class EditGroupServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * Get the parameters in POST and create a group from the values.
   *
   * @param request  provides request information from the HTTP servlet
   * @param response response object where servlet will write information to
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Only logged in users should access this page.
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()
        || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());
    String editType = UtilityClass.getParameter(request, "editType", "");
    // Remove all html tags and trim the spaces in the parameter.
    editType = editType.replaceAll("\\<.*?\\>", "");
    editType = editType.trim();
    logger.atInfo().log("edit type=%s", editType);

    // Check to see if it is create, add member or remove member edit
    if (editType.equals("create")) {
      // Create a group
      String name = UtilityClass.getParameter(request, "name", "");
      name = name.replaceAll("\\<.*?\\>", "");
      name = name.trim();
      logger.atInfo().log("name=%s", name);
      String description = UtilityClass.getParameter(request, "description", "");
      description = description.replaceAll("\\<.*?\\>", "");
      description = description.trim();
      logger.atInfo().log("description=%s", description);
      if (name == "") {
        logger.atWarning().log("Name is null");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Name cannot be null");
        return;
      }
      String ownerID = userService.getCurrentUser().getEmail();
      logger.atInfo().log("owner=%s", ownerID);

      // Set up the new Group and save it in the datastore
      try {
        Entity groupEntity = new Entity("Group");
        groupEntity.setProperty("name", name);
        groupEntity.setProperty("description", description);
        groupEntity.setProperty("ownerID", ownerID);
        groupEntity.setProperty("members", new ArrayList<>());
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(groupEntity);
        logger.atInfo().log("Group: %s , was saved successfully in the datastore",
            groupEntity.getKey().getId());
        response.sendRedirect("/groups?groupID=" + groupEntity.getKey().getId());
      } catch (DatastoreFailureException e) {
        logger.atSevere().log("Error with datastore: %s", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Internal Error occurred when trying to create your group");
        return;
      }
    } else if (editType.equals("add") || editType.equals("remove")) {
      // Add or remove a member to or from a group
      // Get the groupID
      String groupID = UtilityClass.getParameter(request, "groupID", "");
      groupID = groupID.replaceAll("\\<.*?\\>", "");
      groupID = groupID.trim();
      logger.atInfo().log("group=%s", groupID);

      // Get the user to be removed/added
      String email = UtilityClass.getParameter(request, "email", "");
      email = email.replaceAll("\\<.*?\\>", "");
      email = email.trim();
      logger.atInfo().log("email=%s", email);

      Entity groupEntity = new Entity("Group");
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      try {
        Key key = KeyFactory.createKey("Group", Long.parseLong(groupID));
        groupEntity = datastore.get(key);
      } catch (Exception e) {
        logger.atWarning().log("Group ID does not exist");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Group ID does not exist");
        return;
      }
      List<String> members = null;
      try {
        members = (List<String>) groupEntity.getProperty("members");
      } catch (Exception e) {
        logger.atWarning().log("There was an error getting the members list: %s", e);
      }
      try {
        if (members == null) {
          members = new ArrayList<String>();
        }

        if (editType.equals("add")) {
          // Add member
          if (members.contains(email)) {
            // Member is already in the group
            response.sendRedirect("/groups?groupID=" + groupID + "&msg=already_exists");
            return;
          }
          members.add(email);
          groupEntity.setProperty("members", members);
          datastore.put(groupEntity);
          logger.atInfo().log("Group: %s , was saved successfully in the datastore",
              groupEntity.getKey().getId());
          // Member is added to the group
          response.sendRedirect("/groups?groupID=" + groupID + "&msg=add_success");
        } else {
          // Remove member
          if (!members.contains(email)) {
            // Member is not in the group
            response.sendRedirect("/groups?groupID=" + groupID + "&msg=does_not_exist");
            return;
          }
          members.remove(email);
          groupEntity.setProperty("members", members);
          datastore.put(groupEntity);
          logger.atInfo().log("Group: %s , was saved successfully in the datastore",
              groupEntity.getKey().getId());
          // Member is removed from the group
          response.sendRedirect("/groups?groupID=" + groupID + "&msg=remove_success");

        }
      } catch (Exception e) {
        logger.atWarning().log("There was an error editing the members lists: %s", e);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "There was an error editing the members lists");
        return;
      }

    } else {
      // Invalid Edit Type
    }
  }
}
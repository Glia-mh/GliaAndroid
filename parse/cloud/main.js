// The following will return an array of JSON strings for all counselors.
Parse.Cloud.define("getCounselors", function(request, response){
  var query = new Parse.Query("User");
  
  var schoolID= new Parse.Object("SchoolIDs");
  schoolID.id=request.params.schoolObjectId;
  
  query.equalTo("schoolID",schoolID);
  query.find({
    success: function(results){
      var objects = [];
      for (var i = 0; i < results.length; i++){
        objects.push(JSON.stringify(results[i]));
      }
      response.success(objects);
    },
    error: function(){
      response.error("Failed to retrieve counselors.");
    }
  });
});
  
// The following will return an array of JSON strings for all counselors.
Parse.Cloud.define("getSchools", function(request, response){
  var query = new Parse.Query("SchoolIDs");
  query.find({
    success: function(results){
      var objects = [];
      for (var i = 0; i < results.length; i++){
        objects.push(JSON.stringify(results[i]));
      }
      response.success(objects);
    },
    error: function(){
      response.error("Failed to retrieve counselors.");
    }
  });
}); 

// The following will return an array of JSON strings for all counselors.
Parse.Cloud.define("getReportedStudentIDs", function(request, response){
  var query = new Parse.Query("General_Student_IDs");
  query.equalTo("isReported", true);
  var schoolID= new Parse.Object("SchoolIDs");
  schoolID.id=request.params.schoolID;
  query.equalTo("schoolID", schoolID);
  query.find({
    success: function(results){
      var objects = [];
      for (var i = 0; i < results.length; i++){
        objects.push(JSON.stringify(results[i]));
      }
      response.success(objects);
    },
    error: function(){
      response.error("Failed to retrieve reported student IDs.");
    }
  });
}); 
   
   
   
   
// The following cloud function will accept a user ID and return "valid"
// if it is a valid ID or "invalid" if it is not.
Parse.Cloud.define("validateStudentID", function(request, response) {
  var query = new Parse.Query("General_Student_IDs");
  query.equalTo("userID", request.params.userID);
  var schoolID= new Parse.Object("SchoolIDs");
  schoolID.id=request.params.schoolID;
  query.equalTo("schoolID", schoolID);
  query.find({
    success: function(results) {
    if (results.length == 0) {
      response.success("invalid");
      console.log("userID is invalid");
    } else if (results.length == 1) {
      response.success("valid");
      console.log("userID is valid");
    } else {
      response.error("2 or more of the same IDs exist as seperate fields.");
    }
    },
    error: function() {
      response.error("Failed to validate userID.");
    }
  });
});


Parse.Cloud.define("banUsers", function(request, response) {
  if (!request.user) {
    response.error("Must be signed in to call this Cloud Function.");
    return;
  }
  // The user making this request is available in request.user
  // Make sure to first check if this user is authorized to perform this change.
  // One way of doing so is to query an Admin role and check if the user belongs to that Role.
  // Replace !authorized with whatever check you decide to implement.
  if (!request.user.attributes.counselorType != "0") {
    response.error("Not an Admin.");
    return;    
  }

  // The rest of the function operates on the assumption that request.user is *authorized*

  Parse.Cloud.useMasterKey();

  // Query for the user to be modified by username
  // The username is passed to the Cloud Function in a 
  // key named "username". You can search by email or
  // user id instead depending on your use case.
  var email_array = request.params.counselors;
  var mainQuery = new Parse.Query(Parse.User);
  mainQuery.equalTo("email",email_array[0]);
  mainQuery.equalTo("schoolID", request.user.attributes.schoolID);
  for(var i=1; i<email_array.length; ++i){
    var query = new Parse.Query(Parse.User);
    query.equalTo("email",email_array[i]);
    query.equalTo("schoolID", request.user.attributes.schoolID);
    mainQuery= Parse.Query.or(mainQuery, query);
  }

  mainQuery.find({
  success: function(counselors) {
    
    if(counselors.length != email_array.length){
      response.error('You seem to have mispelled one or more of the accounts that you want to disable, or that account has already been removed!');
      return;
    }


    counselors.forEach(function(counselor){
      counselor.set("rootsAuthData", "banned");
    });
    Parse.User.saveAll(counselors, {
        success: function(anotherUser) {
          response.success(counselors);
        },
        error: function(error) {
          repsonse.error('There seems to be a problem with our website. Email us at <a href="mailto:teamroots@teamroots.org">teamroots@teamroots.org</a>. With the following information: <br>  Error: ' + error.code + ', ' + error.message);
        }
      });

    
    
  }, 

  error: function(error) {
    repsonse.error('There seems to be a problem with our website. Email us at <a href="mailto:teamroots@teamroots.org">teamroots@teamroots.org</a>. With the following information: <br>  Error: ' + error.code + ', ' + error.message);
  }
});

});

  
  Parse.Cloud.define("changeStudentReportValue", function(request, response) {
  var query = new Parse.Query("General_Student_IDs");
  query.equalTo("userID", request.params.userID);
  query.find({
    success: function(results) {
    results[0].set("isReported",!results[0].get("isReported"));
      results[0].save();
      response.success(results[0]);
    },
    error: function() {
      response.error("Failed to change student report info.");
    }
  });
});

// The following cloud function will change a given counselors availability state
// to be available
Parse.Cloud.define("setCounselorStateToAvailable", function(request, response) {
  Parse.Cloud.useMasterKey();
  var query = new Parse.Query("User");
  query.equalTo("objectId", request.params.userID);
  query.find({
    success: function(results) {
    if (results.length == 0) {
      response.error("Given user ID not found. Not registed yet?");
    } else if (results.length == 1) {
      results[0].set("isAvailable",true);
      results[0].save()
      response.success("Success!");
    } else {
      response.error("2 or more of the same IDs exist as seperate fields.");
    }
    },
    error: function() {
      response.error("Failed to change counselor state.");
    }
  });
});
  
  
  
  
// The following cloud function will change a given counselors availability state
// to be unavailable
Parse.Cloud.define("setCounselorStateToUnavailable", function(request, response) {
  Parse.Cloud.useMasterKey();
  var query = new Parse.Query("User");
  query.equalTo("objectId", request.params.userID);
  query.find({
    success: function(results) {
    if (results.length == 0) {
      response.error("Given user ID not found. Not registed yet?");
    } else if (results.length == 1) {
      results[0].set("isAvailable",false);
      results[0].save();
      response.success("Success!");
    } else {
      response.error("2 or more of the same IDs exist as seperate fields.");
    }
    },
    error: function() {
      response.error("Failed to change counselor state.");
    }
  });
});
   
   
   
   
// The following before save hook will only save an ID to General_Student_IDs
// if the ID does not already exist.
Parse.Cloud.beforeSave("General_Student_IDs", function(request, response){
  var query = new Parse.Query("General_Student_IDs");
  query.equalTo("userID", request.object.get("userID"));
  query.equalTo("isReported", request.object.get("isReported"));
  query.find({
    success: function(results) {
      if (results.length == 0) {
        response.success();
      } else {
        response.error("This ID is already registered.");
      }
    },
    error: function(){
   
    }
  });
});
  

Parse.Cloud.afterSave(Parse.User, function(request) {
  // Our "Comment" class has a "text" key with the body of the comment itself
  var  isAvailable = request.object.get('isAvailable');
  var  userID= request.object.id;
  var pushQuery = new Parse.Query(Parse.Installation);
  pushQuery.equalTo('deviceType', 'android');

  Parse.Push.send({
    where: pushQuery, // Set our Installation query
    data: {
      alert:  isAvailable.toString(),
    userID: userID.toString()
    }
  }, {
    success: function() {
      // Push was successful
    console.log("Pushed availability " + isAvailable.toString());
    },
    error: function(error) {
      throw "Got an error " + error.code + " : " + error.message;
    }
  });
});
  
// The following before save hook will only save an ID to User
// if the ID does not already exist.
Parse.Cloud.beforeSave("User", function(request, response){
  var query = new Parse.Query("User");
  query.equalTo("userID", request.object.get("userID"));
  query.find({
    success: function(results) {
      if (results.length == 0) {
        response.success();
      } else {
        response.error("This ID is already registered.");
      }
    },
    error: function(){
   
    }
  });
});
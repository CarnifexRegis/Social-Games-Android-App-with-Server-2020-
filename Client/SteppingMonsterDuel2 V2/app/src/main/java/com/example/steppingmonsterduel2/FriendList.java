package com.example.steppingmonsterduel2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Objects.Friends;
import com.example.steppingmonsterduel2.Objects.User;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.example.steppingmonsterduel2.Util.HttpPoster;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class FriendList extends AppCompatActivity{



    //Set all the maps to store frined information friednrequests, traderequests and matchrequests that you know
    //if one needs to update or not
    HashMap<Integer, Friends> friendMap = new HashMap<>();
    HashMap<Integer, Friends> friendRequestMap = new HashMap<>();
    HashMap<Integer, Friends> layoutContains = new HashMap<>();
    HashMap<Integer, User> matchRequest = new HashMap<>();
    HashMap<Integer, User> tradeRequests = new HashMap<>();
    ArrayList<User> acceptMatch;
    ArrayList<User> acceptTrade;
    LinearLayout friendLayout;

    TextView ownerName;
    ImageView backToMenu;
    TextView userID;

    //Used to set an image to the players profile
    Uri chosenImage;
    ImageView ownerImage;
    //Stored to know whether we need to ask for permission again or not
    static int REQCODE = 1;
    static int MY_RESULT_ACCESS_FINE =1;
    final Handler handler = new Handler();
    Runnable run;
    public boolean updateRequests = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        friendLayout = findViewById(R.id.friendlayout);
        ownerName = findViewById(R.id.YourUsername);
        backToMenu = findViewById(R.id.GoBackToMenu);
        userID = findViewById(R.id.UserID);
        if(Configuration.currentUser==null){
            Intent intent = new Intent(FriendList.this,LoginActivity.class);
            startActivity(intent);
        }

        //set user information as name id and image
        setUserInformation();


        //call server every second to get updates...
        //Todo by switching to other intents always deleted the handler
        refresh();

        //add users by clicking on the button
        addUser();

        //refresh the activity
        refreshButton();
    }


    /*
     inspired from https://stackoverflow.com/questions/1921514/how-to-run-a-runnable-thread-in-android-at-defined-intervals
     */
    //refresh refreshing the activity every few seconds to get updates
    private void refresh(){
         run = new Runnable() {
            public void run() {
                refreshButton();
                handler.postDelayed(this, Configuration.REFRESH_TIME);
            }
        };

        handler.postDelayed(run, Configuration.REFRESH_TIME);
    }


    //Sets the user information as the pictureans the id
    private void setUserInformation(){
        ownerImage = findViewById(R.id.YourPicture);
        //Initialize userImage and UserView


        ownerName.setText(Configuration.currentUser.getName());

        //setPicture user
        if(Configuration.currentUser.getPicture().equals("null")){
            ownerImage.setImageResource(R.drawable.user_icon2);
        } else{
            Configuration.getPictureOutOfStorageAndSetItToView(FriendList.this, ownerImage,Configuration.currentUser.getPicture());
        }
        userID.setText("ID: "+ Configuration.currentUser.getId());

        ownerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showStatsDialog( Configuration.currentUser.getName(),true,null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour(FriendList.this,1,true);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour(FriendList.this,2,true);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour(FriendList.this,0,true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour(FriendList.this,3,true);
                }
            }
        });

        //by pressing, return back to menu
        backToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent homeIntent = new Intent(FriendList.this,HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(homeIntent);
            }
        });

        //Set a new photo to your profile
        ownerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=22) {
                    checkForPermission();
                } else{
                    chosePicture();
                }
            }
        });
    }

    //Checks for permission if storage can be accessed
    private void checkForPermission(){
        if(ContextCompat.checkSelfPermission(FriendList.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            //if(!ActivityCompat.shouldShowRequestPermissionRationale(FriendList.this,Manifest.permission.READ_EXTERNAL_STORAGE))
                ActivityCompat.requestPermissions(FriendList.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_RESULT_ACCESS_FINE);
        } else{
            chosePicture();
        }
    }

    //Method to send a request to a friend
    private void addUser(){
        ImageView addUser = findViewById(R.id.addUserButton);
        final TextView idOfUser = findViewById(R.id.addUserField);

        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idOfUser.getText().toString();
                //check if field is empty
                if(id.isEmpty()){
                    Toast.makeText(FriendList.this,"Sie haben einen leeren Namen eingegeben!",Toast.LENGTH_SHORT).show();
                } else {
                    //if field is not empty search for ID
                    Toast.makeText(FriendList.this,"Suche nach dem User: "+id+" hat erfolgreich begonnen!",Toast.LENGTH_SHORT).show();

                    //Check if there are no other characters as digits
                    int j=0;
                    boolean Parsable = false;
                    while(j<id.length()){
                        if (((int)id.charAt( j )) > 57||((int)id.charAt( j )) < 48) {
                            break;
                        }
                        if(j==id.length()-1){
                            Parsable = true;
                        }
                        j++;
                    }
                    if(Parsable) {

                        //If you entered ur id nothing happens, otherwise you will add the friend
                        //if friend already exsits then nothing happens
                        if(Integer.parseInt(id)==Configuration.currentUser.getId()) {
                            Toast.makeText( FriendList.this, "Sie haben Ihre ID eingegeben!", Toast.LENGTH_SHORT ).show();
                        } else {
                            HttpGetter addUser = new HttpGetter();
                            addUser.execute( "search", "" + Configuration.currentUser.getId(), "" + Integer.parseInt( id ), "Friend" );
                            try {

                                JSONObject jsonObject = new JSONObject( addUser.get( Configuration.timeoutTime, TimeUnit.SECONDS ) );
                                int result = jsonObject.getInt( "Worked" );

                                if (result == 1) {
                                    Toast.makeText( FriendList.this, "Your friend has been successfully added!", Toast.LENGTH_SHORT ).show();
                                } else if (result == 0) {
                                    Toast.makeText( FriendList.this, "This friend already exists!", Toast.LENGTH_SHORT ).show();
                                } else {
                                    Toast.makeText( FriendList.this, "Some error occured by searching friend!", Toast.LENGTH_SHORT ).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Configuration.serverDownBehaviour( FriendList.this, 3 ,true);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                                Configuration.serverDownBehaviour( FriendList.this, 2 ,true);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Configuration.serverDownBehaviour( FriendList.this, 1 ,true);
                            } catch (TimeoutException e) {
                                e.printStackTrace();
                                Configuration.serverDownBehaviour( FriendList.this, 0 ,true);
                            }
                        }
                        //If ID does not exist just type in wrong id
                    } else{
                        Toast.makeText( FriendList.this, "Wrong ID!", Toast.LENGTH_SHORT ).show();
                    }
                }
            }
        });
    }

    //Chose picture out of storage
    private void chosePicture() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,REQCODE);
    }

    //Set picutre to view working exactly the same as in editcards therefore check out editcards onactivityresult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQCODE&&resultCode==RESULT_OK&&data!=null){
            try {
                chosenImage = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),chosenImage);
                StorageReference storageRef = Configuration.storage.getReference();
                System.err.println(chosenImage.toString());
                final String path = "userPhotos/profileIcon"+Configuration.currentUser.getId()+".jpg";
                final StorageReference imgRef = storageRef.child(path);

                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
                byte[] bytes = byteArray.toByteArray();

                final UploadTask uploadTask = imgRef.putBytes(bytes);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(FriendList.this,"Picture could not be stored in cloud!",Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //set picture
                        ownerImage.setImageURI(chosenImage);
                        Configuration.currentUser.setPicture(path);
                        //Save imagepath in database
                        HttpGetter storePicture = new HttpGetter();
                        String pathChanged = path.replace('/',',');
                        System.out.println(pathChanged);
                        storePicture.execute("save",""+Configuration.currentUser.getId(),pathChanged,"Picture");
                        try {
                            String getPid = storePicture.get(Configuration.timeoutTime,TimeUnit.SECONDS);
                            JSONObject jsonObject = new JSONObject(getPid);
                            //bind imagepath to user
                            HttpGetter getIfWorked = new HttpGetter();
                            getIfWorked.execute("save",""+jsonObject.getInt("Pid"),""+Configuration.currentUser.getId(),"Pid");
                            String worked = getIfWorked.get(Configuration.timeoutTime,TimeUnit.SECONDS);
                            JSONObject jsonObject1 = new JSONObject(worked);
                            if(jsonObject1.getBoolean("Worked?")) {
                                Toast.makeText(FriendList.this, "Linking picture worked!", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(FriendList.this, "Linking picture failed!", Toast.LENGTH_SHORT).show();
                            }
                            jsonObject.getInt("Pid");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(FriendList.this,1,true);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(FriendList.this,2,true);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(FriendList.this,0,true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(FriendList.this,3,true);
                        }


                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //fill the freinds tothe list and check if there is a request or not
    private void fillFriendListWithFriendsAndCheckForTradeOrRemove()
    {
        //Parent friendlayout
        // Layout inflater
        final LayoutInflater friendInflater = getLayoutInflater();


        //Add requests to the scrollview
        for(Integer key: friendRequestMap.keySet()) {

            //check if friend is already in the layout
            //if not add it
            if(!layoutContains.containsKey(friendRequestMap.get(key).getIid())) {
                View view = friendInflater.inflate( R.layout.requestview, friendLayout, false );

                //initialize the views
                final LinearLayout request = view.findViewById( R.id.friendRequest );
                final TextView requestText = view.findViewById( R.id.RequestName );
                final ImageView requestImg = view.findViewById( R.id.RequestPicture );

                final String userName = friendRequestMap.get( key ).getName();
                final int id = friendRequestMap.get( key ).getIid();
                final Friends friendReq2 = friendRequestMap.get( key );
                requestText.setText( userName );

                //set picture to default with no picture in array
                if (friendReq2.getPicture() .equals("null")) {
                    requestImg.setImageResource( R.drawable.user_icon2 );
                } else {
                    Configuration.getPictureOutOfStorageAndSetItToView(FriendList.this, requestImg, friendRequestMap.get( key ).getPicture() );
                }
                //add view to scrollview
                friendLayout.addView( request );
                layoutContains.put( id, friendReq2 );

                ImageView accept = view.findViewById( R.id.acceptFriend );
                ImageView decline = view.findViewById( R.id.declineFriend );

                //if you click on accept user will be shown in friendlist
                accept.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        HttpGetter acceptFriend = new HttpGetter();
                        acceptFriend.execute( "accept", "" + id, "" + Configuration.currentUser.getId(), "Friend" );

                        try {
                            JSONObject jsonObject = new JSONObject( acceptFriend.get( Configuration.timeoutTime, TimeUnit.SECONDS ) );
                            boolean result = jsonObject.getBoolean( "FriendAccepted" );

                            if (result) {
                                //if friend has been successfully added update list
                                Toast.makeText( FriendList.this, userName + " has been successfully added to friendlist!", Toast.LENGTH_SHORT ).show();
                                //remove request view from scrollview
                                friendRequestMap.remove( friendReq2.getIid() );
                                layoutContains.remove( friendReq2.getIid() );
                                friendLayout.removeView( request );

                                //update friends

                            } else {
                                Toast.makeText( FriendList.this, userName + "could not be added to friendlist", Toast.LENGTH_SHORT ).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour( FriendList.this, 3 ,true);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour( FriendList.this, 2 ,true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour( FriendList.this, 1 ,true);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour( FriendList.this, 0 ,true);
                        }

                        View view2 = friendInflater.inflate( R.layout.friendview, friendLayout, false );
                        final LinearLayout friend = view2.findViewById( R.id.friendEntry );
                        final TextView friendsText = view2.findViewById( R.id.UsernameText );
                        final ImageView friendsImg = view2.findViewById( R.id.UsernamePicture );

                        friendsText.setText( userName );
                        //set picture
                        if (friendReq2.getPicture().equals("null")) {
                            friendsImg.setImageResource( R.drawable.user_icon2 );
                        } else {
                            Configuration.getPictureOutOfStorageAndSetItToView(FriendList.this, friendsImg, friendReq2.getPicture());
                        }
                        friendLayout.addView( friend );
                        friendMap.put( id, friendReq2 );
                        layoutContains.put( id,friendReq2 );


                        //Button to delete a user
                        ImageView userDelete = view2.findViewById( R.id.RemoveFriendButton );
                        //Button to send a trade request to the user
                        ImageView userTrade = view2.findViewById( R.id.tradeFriendButton );
                        //Button to send a game request to a friend
                        ImageView userDuel = view2.findViewById( R.id.duelFriendButton );


                        setOnClickButtonsForDuelTradeAndDelete( userDuel, userTrade, userDelete, friendsText, requestText.getText().toString(), friendLayout, friend, id, friendReq2 );

                    }
                } );

                //If you decline a request the request will be rejected and removed
                decline.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteUser( id, userName, friendLayout, request, friendRequestMap );
                    }

                } );
            }
        }

        //Add friends to the scrollview
        for(Integer key : friendMap.keySet()) {


            if (!layoutContains.containsKey( friendMap.get( key ).getIid() )) {

                //initilaize every view a friend needs
                View view = friendInflater.inflate( R.layout.friendview, friendLayout, false );
                final LinearLayout friend = view.findViewById( R.id.friendEntry );
                final TextView usernameText = view.findViewById( R.id.UsernameText );
                final ImageView usernameImg = view.findViewById( R.id.UsernamePicture );
                final int friendId = friendMap.get( key ).getIid();
                final Friends friendReq = friendMap.get( key );
                usernameText.setText( friendMap.get( key ).getName() );

                //set img to default with string not filled
                if (friendMap.get( key ).getPicture().equals("null")) {
                    usernameImg.setImageResource( R.drawable.user_icon2 );
                } else {
                    Configuration.getPictureOutOfStorageAndSetItToView(FriendList.this, usernameImg, friendMap.get( key ).getPicture() );
                }
                friendLayout.addView( friend );
                layoutContains.put(friendId,friendReq);

                //Button to delete a user
                ImageView userDelete = view.findViewById( R.id.RemoveFriendButton );
                //Button to send a trade request to the user
                ImageView userTrade = view.findViewById( R.id.tradeFriendButton );
                //Button to send a game request to a friend
                ImageView userDuel = view.findViewById( R.id.duelFriendButton );

                setOnClickButtonsForDuelTradeAndDelete( userDuel, userTrade, userDelete, usernameText, friendMap.get( key ).getName(), friendLayout, friend, friendId, friendReq );
            }
        }
    }




    //If one user is to bad to play with u can just remove him from the list
    private void deleteUser(int idOfFriend,String userName, LinearLayout friendLayout, LinearLayout element,HashMap<Integer,Friends> list){
        HttpGetter deleteFriend = new HttpGetter();
        deleteFriend.execute("delete",""+Configuration.currentUser.getId(),""+idOfFriend,"Friend");
        try {
            JSONObject jsonObject = new JSONObject(deleteFriend.get(Configuration.timeoutTime,TimeUnit.SECONDS));
            boolean result = jsonObject.getBoolean("FriendDeleted");

            if(result){
                //if friend has been successfully deleted update list
                Toast.makeText(FriendList.this,userName+" has been successfully deleted!",Toast.LENGTH_SHORT).show();
                //remove request view from scrollview
                list.remove(idOfFriend);
                friendLayout.removeView(element);
                layoutContains.remove(idOfFriend);
                //update friends
            }else{
                Toast.makeText(FriendList.this,"User could not get deleted!",Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Configuration.serverDownBehaviour(FriendList.this,3,true);
            e.printStackTrace();
        } catch (ExecutionException e) {
            Configuration.serverDownBehaviour(FriendList.this,2,true);
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(FriendList.this,1,true);
        } catch(TimeoutException e){
            e.printStackTrace();
            Configuration.serverDownBehaviour(FriendList.this,0,true);
        }
    }

    //get every traderequest from the server
    private void getTradeRequests() throws InterruptedException, ExecutionException, TimeoutException, JSONException {
        HttpGetter getTrades = new HttpGetter();
        getTrades.execute(""+Configuration.currentUser.getId(),"trade");


        JSONObject json = new JSONObject(getTrades.get(Configuration.timeoutTime, TimeUnit.SECONDS));
        JSONArray jsonUsers = json.getJSONArray("TradeRequests");

        for(int i=0;i<jsonUsers.length();i++)
        {
            JSONObject user = jsonUsers.getJSONObject(i);

            if(!tradeRequests.containsKey(user.getInt("ID"))) {
                tradeRequests.put( user.getInt( "ID" ), new User( user.getInt( "ID" ), user.getInt( "Steps" )
                        , user.getString( "Name" ), user.getInt("Wins"), user.getInt("Loses"), user.getString( "Picture" ) ) );
                updateRequests= true;

            }
        }
    }

    //get every matchrequest from the server
    private void getMatchRequests() throws InterruptedException, ExecutionException, TimeoutException, JSONException {

        HttpGetter getMatches = new HttpGetter();
        getMatches.execute(""+Configuration.currentUser.getId(),"match");


        JSONObject json = new JSONObject(getMatches.get(Configuration.timeoutTime, TimeUnit.SECONDS));
        JSONArray jsonUsers = json.getJSONArray("MatchRequests");

        for(int i=0;i<jsonUsers.length();i++)
        {
            JSONObject user = jsonUsers.getJSONObject(i);

            if(!matchRequest.containsKey(user.getInt("ID"))) {
                matchRequest.put( user.getInt( "ID" ), new User( user.getInt( "ID" ), user.getInt( "Steps" )
                        , user.getString( "Name" ), user.getInt("Wins"), user.getInt("Loses"), user.getString( "Picture" ) ) );
                updateRequests = true;
            }
        }
    }


    //add friends and requests to the hashmaps to store them and to compare them afterwards if there needs to be done an update
    private void addFriendsAndRequests() throws ExecutionException, InterruptedException, JSONException, TimeoutException {
        //refresh the lists

        friendMap = new HashMap<>();
        friendRequestMap = new HashMap<>();
        HttpGetter getFriends = new HttpGetter();
        getFriends.execute(""+Configuration.currentUser.getId(),"getFriends");


        JSONObject json = new JSONObject(getFriends.get(Configuration.timeoutTime, TimeUnit.SECONDS));
        JSONArray jsonUsers = json.getJSONArray("Friends");

        for(int i=0;i<jsonUsers.length();i++)
        {
            JSONObject friends = jsonUsers.getJSONObject(i);

            Friends friend = new Friends(friends.getInt("ID"),friends.getInt("Accepted")
                    ,friends.getString("Name"),friends.getString("Picture"),
                    friends.getInt("Fid"),0,0,0, friends.getInt("Steps"),friends.getInt("Winrate"));

                if (friends.getInt( "Accepted" ) == 0) {
                    friendRequestMap.put( friends.getInt( "ID" ), friend);
                } else {
                    friendMap.put( friends.getInt( "ID" ), friend);
                }
        }

        boolean checkForRefresh = false;
        for(Integer key: layoutContains.keySet()){
            if(!friendRequestMap.containsKey(layoutContains.get(key).getIid())&&!friendMap.containsKey(layoutContains.get(key).getIid())) {
                checkForRefresh = true;
            }
        }
        if(checkForRefresh){
            layoutContains = new HashMap<>();
            friendLayout.removeAllViews();
        }
    }


    //show a dialog by pressing on the users name
    //by clicking on the ? the quest dialog will show up
    private void showStatsDialog(String name,boolean isUser, final Friends friend) throws InterruptedException, ExecutionException, TimeoutException, JSONException {
        final Dialog alert = new Dialog(FriendList.this);
        alert.setContentView(R.layout.stats_dialog);
        TextView statsUpdate = alert.findViewById(R.id.StatisticsLabel);
        TextView winRateUser = alert.findViewById(R.id.WinrateUserDialog);
        TextView stepsUser = alert.findViewById(R.id.StepsUserDialog);
        Button removeDialog = alert.findViewById(R.id.RemoveStats);
        final ImageView image = alert.findViewById((R.id.quest));

        if(isUser){
            image.setImageDrawable(null);
            winRateUser.setText(winRateUser.getText()+" "+Configuration.currentUser.getWinRate()+"%");
            statsUpdate.setText("Statistics of "+ name + "!");
            stepsUser.setText(stepsUser.getText()+" "+Configuration.currentUser.getSteps());
            alert.show();
        }



        //dismiss the dialog
        removeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        if(!isUser) {
            if(friend==null) {
                alert.show();
                new AlertDialog.Builder(FriendList.this)
                        .setTitle("Quest:")
                        .setMessage("Quest not working!")
                        .setPositiveButton("Got it!", null).show();
            } else{
                //set information of the user as the win percentage and the amount of steps made
                //by clicking on ? he will get the questscore of the friend tupel
                winRateUser.setText(winRateUser.getText()+" "+friend.getWinrate()+"%");
                statsUpdate.setText("Statistics of "+ name + "!");
                stepsUser.setText(stepsUser.getText()+" "+friend.getSteps());

                HttpGetter getQuest = new HttpGetter();
                getQuest.execute(""+friend.getFid(),"quest");
                String resultQuest = getQuest.get(Configuration.timeoutTime,TimeUnit.SECONDS);
                JSONObject json = new JSONObject(resultQuest);
                //Check if quest is done
                int trades = json.getInt("TempTrade") - json.getInt("Trade");
                int matches = json.getInt("TempMatch") - json.getInt("Match");
                final int score = json.getInt("Score");
                String worked = json.getString("Update?");
                //if so then the packs must be reloaded
                if(worked.equals("Yes")){
                    //pull user unopened packs from server
                    HttpGetter getPacksRequest = new HttpGetter();
                    getPacksRequest.execute("get", "" + Configuration.currentUser.getId(), "Packs");

                    JSONObject jsonPackResponse = new JSONObject(getPacksRequest.get(Configuration.timeoutTime, TimeUnit.SECONDS));

                    JSONArray packTypes = jsonPackResponse.getJSONArray("PackTypes");

                    OpenPacksActivity.packsTypesStack = new LinkedList<Integer>();
                    OpenPacksActivity.numberOfPacks = packTypes.length();
                    for (int i = 0; i < packTypes.length(); i++) {
                        JSONObject pack = packTypes.getJSONObject(i);
                        OpenPacksActivity.packsTypesStack.add(pack.getInt("Type"));
                    }
                    //tell the user that he got a reward
                    Toast.makeText(FriendList.this,"You got a friendreward!\nSteps and Packs are added!",Toast.LENGTH_LONG).show();
                }
                //Show the progress of the quest if everything is 0 then show an !. This means that quest was already done for this month
                if(trades<0)
                    trades =0;
                if(matches<0)
                    matches = 0;
                if(trades ==0&&matches==0) {
                    image.setImageResource(R.drawable.quest_done);
                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alert.dismiss();
                            new AlertDialog.Builder(FriendList.this)
                                    .setTitle("Quest:")
                                    .setMessage("No quests available!\n" +
                                            "Your total score*: "+score+"!\n\n"+
                                            "*Score will be increased if monthly quests are completed!")
                                    .setPositiveButton("Got it!", null).show();
                        }
                    });
                    alert.show();
                }else{
                    alert.show();
                    final int restTrades = trades;
                    final int restMatches = matches;
                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alert.dismiss();
                            new AlertDialog.Builder(FriendList.this)
                                    .setTitle("Quest:")
                                    .setMessage("Trade "+ restTrades + " times and duel "+restMatches+" " +
                                            "times against "+ friend.getName()+" to finish the quest!\n" +
                                            "Total score*: "+score+"!\n\n"+
                                            "*Score will be increased if monthly quests are completed!")
                                    .setPositiveButton("Got it!", null).show();
                        }
                    });
                }
            }
        }
    }


    //By clicking on the trade button or on the deul button the friend will get notified that someone sent a request
    private void setOnClickButtonsForDuelTradeAndDelete(View duel, View trade, View remove,
                                                        View userStats, final String userName,
                                                        final LinearLayout scroll,
                                                        final LinearLayout friend,final int id,final Friends friends) {

        duel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //By accepting the request will be sent
                                        new AlertDialog.Builder(FriendList.this)
                                                .setTitle("Send duel request to friend!")
                                                .setMessage("Are you sure that you want to send a duel request to "+userName+"?")
                                                .setPositiveButton("No", null)
                                                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        HttpGetter tradeReq = new HttpGetter();
                                                        tradeReq.execute("set", "" + Configuration.currentUser.getId(), "" + friends.getFid(), "MatchReq");
                                                        try {
                                                            String worked = tradeReq.get(Configuration.timeoutTime, TimeUnit.SECONDS);
                                                            if (worked == "Worked") {
                                                                Toast.makeText(FriendList.this, "Request sent!", Toast.LENGTH_SHORT);
                                                            } else {
                                                                Toast.makeText(FriendList.this, "Request failed!", Toast.LENGTH_SHORT);
                                                            }
                                                        } catch (ExecutionException e) {
                                                            e.printStackTrace();
                                                            Configuration.serverDownBehaviour(FriendList.this, 2,true);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                            Configuration.serverDownBehaviour(FriendList.this, 1,true);
                                                        } catch (TimeoutException e) {
                                                            e.printStackTrace();
                                                            Configuration.serverDownBehaviour(FriendList.this, 0,true);
                                                        }
                                                    }
                                                }).show();
                                    }
                                });


        //by clicking on username of freind stats will show up
                userStats.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            showStatsDialog(userName, false, friends);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(FriendList.this, 1,true);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(FriendList.this, 2,true);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(FriendList.this, 0,true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(FriendList.this, 3,true);
                        }


                    }
                });

        //If delete Button gets clicked remove user
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(FriendList.this)
                        .setTitle("Remove Friend!")
                        .setMessage("Are you sure that you want to remove "+userName+"?")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUser(id,userName,scroll,friend,friendMap);
                            }
                        })
                        .setPositiveButton("No", null).show();

            }
        });

        //if trade Button is getting clicked send trade request!
        trade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(FriendList.this)
                        .setTitle("Send trade request to friend!")
                        .setMessage("Are you sure that you want to send a trade request to "+userName+"?")
                        .setPositiveButton("No", null)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HttpGetter tradeReq = new HttpGetter();
                                tradeReq.execute("set", "" + Configuration.currentUser.getId(), "" + friends.getFid(), "TradeReq");
                                try {
                                    String worked = tradeReq.get(Configuration.timeoutTime, TimeUnit.SECONDS);
                                    if (worked .equals("Worked")) {
                                        Toast.makeText(FriendList.this, "Request sent!", Toast.LENGTH_SHORT);
                                    } else {
                                        Toast.makeText(FriendList.this, "Request failed!", Toast.LENGTH_SHORT);
                                    }
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                    Configuration.serverDownBehaviour(FriendList.this, 2,true);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    Configuration.serverDownBehaviour(FriendList.this, 1,true);
                                } catch (TimeoutException e) {
                                    e.printStackTrace();
                                    Configuration.serverDownBehaviour(FriendList.this, 0,true);
                                }
                            }
                        }).show();
            }
        });
    }

    //get the requests from the server
    private void getRequests(){
        if(tradeRequests!=null) {
            //if there is a traderequest and u accept it u will jump to the new activitiy roles are given to the users
            for (Integer key: tradeRequests.keySet()) {
                final int index = key;
                final int id = tradeRequests.get(key).getId();
                final String nameTrade = tradeRequests.get(index).getName();
                final String pictureTrade = tradeRequests.get(index).getPicture();
                new AlertDialog.Builder(FriendList.this)
                        .setTitle("Trade request!")
                        .setMessage("Are you sure that you want to trade with " + tradeRequests.get(key).getName() + "?")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HttpPoster acceptTrade = new HttpPoster();
                                acceptTrade.execute("accept",""+Configuration.currentUser.getId(),""+id,"Trade");

                                tradeRequests.remove(id);
                                Intent intent = new Intent(FriendList.this, SwapCardActivity.class);
                                Bundle deckInfo = new Bundle();

                                //give user roles and set the id as the picutre of the friend
                                deckInfo.putString("Role","User");
                                deckInfo.putInt("Trader",id);
                                deckInfo.putString("Name",nameTrade);
                                deckInfo.putString("Picture",pictureTrade);

                                intent.putExtras(deckInfo);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setPositiveButton( "No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //by clicking no u reject the request and it will be removed
                                HttpPoster tradeReq = new HttpPoster();
                                tradeReq.execute("" + Configuration.currentUser.getId(), "" + id, "resetTrade");
                                tradeRequests.remove(id);
                            }
                        } ).show();
            }
        }


        //Match Requests working the same as the traderequest
        if(matchRequest!=null) {
            for (Integer key: matchRequest.keySet()) {
                final User opponent = matchRequest.get(key);
                new AlertDialog.Builder(FriendList.this)
                        .setTitle("Duel request!")
                        .setMessage(opponent.getName()+" wants to duel!")
                        .setNegativeButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //HttpPoster.quickPost("GameManagement", "Instantiate", Configuration.currentUser.getId(), opponent.getId()); Now part of accept/:userID/:opponentID/Match
                                HttpPoster acceptMatch = new HttpPoster();
                                acceptMatch.execute("accept",""+Configuration.currentUser.getId(),""+opponent.getId(),"Match");
                                matchRequest.remove(opponent.getId());

                                actuallyStartMatchLobbyActivity(opponent);
                            }
                        })
                        .setPositiveButton( "Decline", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HttpPoster tradeReq = new HttpPoster();
                                tradeReq.execute("" + Configuration.currentUser.getId(), "" + opponent.getId(), "resetMatch");

                                matchRequest.remove( opponent.getId() );
                            }
                        } ).show();
            }
        }
    }

    //if someone accepted ur traderequest u will be directed into the new activity
    private void tryStartTrade(){
        if(acceptTrade!=null&&acceptTrade.size()!=0){
            int tradeID = acceptTrade.get(0).getId();

            HttpPoster tradeReq = new HttpPoster();
            tradeReq.execute("" + Configuration.currentUser.getId(), "" + tradeID, "resetTrade");


            HttpGetter getTradeHappening = new HttpGetter();
            getTradeHappening.execute("test",""+tradeID,""+Configuration.currentUser.getId(),"TradeHappening");

            try {
                String result = getTradeHappening.get(Configuration.timeoutTime,TimeUnit.SECONDS);
                if(result.equals("Worked")){
                    Intent intent = new Intent(FriendList.this,SwapCardActivity.class);

                    Bundle deckInfo = new Bundle();

                    //Set the informations to the tradeactivity as the opponent name, picture, id
                    deckInfo.putString("Role","Opponent");
                    deckInfo.putInt("Trader",tradeID);
                    deckInfo.putString("Name",acceptTrade.get(0).getName());
                    deckInfo.putString("Picture",acceptTrade.get(0).getPicture());

                    intent.putExtras(deckInfo);
                    //directed into the new activity
                    startActivity(intent);
                    finish();
                } else{
                    Toast.makeText(FriendList.this,"Something went wrong with your friendrequest!"
                            ,Toast.LENGTH_SHORT).show();
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
                Configuration.serverDownBehaviour(FriendList.this,2,true);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Configuration.serverDownBehaviour(FriendList.this,1,true);
            } catch (TimeoutException e) {
                e.printStackTrace();
                Configuration.serverDownBehaviour(FriendList.this,0,true);
            }
        }
    }

    //Match Requests
    private void tryStartMatchLobby(){
        if(acceptMatch!=null&&acceptMatch.size()!=0){
            User opponent = acceptMatch.get(0);

            HttpPoster matchReq = new HttpPoster();
            matchReq.execute("" + Configuration.currentUser.getId(), "" + opponent.getId(), "resetMatch");

            actuallyStartMatchLobbyActivity(opponent);
        }
    }

    //Starting the match
    private void actuallyStartMatchLobbyActivity(User opponent){

        Intent intent = new Intent(FriendList.this, ChooseDuelDeckActivity.class);
        intent.putExtra("Opponent", opponent);
        startActivity(intent);
        finish();
    }

    //refreshing everything what is in here every few seconds //seconds are defined in configuration
    private void refreshButton(){
                try {
                    addFriendsAndRequests();
                    getMatchRequests();
                    getTradeRequests();
                    getAcceptedMatches();
                    getAcceptedTrades();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour(FriendList.this,3,true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour(FriendList.this,2,true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour(FriendList.this,1,true);
                } catch(TimeoutException e){
                    e.printStackTrace();
                    Configuration.serverDownBehaviour(FriendList.this,0,true);
                }

                //Manage friends add them to list and handle deletions and trading + adding requests
                fillFriendListWithFriendsAndCheckForTradeOrRemove();

                if(updateRequests) {
                    getRequests();
                    updateRequests=false;
                }

                tryStartMatchLobby();
                tryStartTrade();
    }


    //get all the accepted matches from the server
    private void getAcceptedMatches() throws JSONException, InterruptedException, ExecutionException, TimeoutException {
        acceptMatch = new ArrayList<>();
        HttpGetter getAcceptedMatches = new HttpGetter();
        getAcceptedMatches.execute(""+Configuration.currentUser.getId(),"AcceptedMatches");


        JSONObject json = new JSONObject(getAcceptedMatches.get(Configuration.timeoutTime, TimeUnit.SECONDS));
        JSONArray jsonUsers = json.getJSONArray("AcceptedMatches");

        for(int i=0;i<jsonUsers.length();i++)
        {
            JSONObject acceptedMatches = jsonUsers.getJSONObject(i);

            acceptMatch.add(new User(acceptedMatches.getInt("ID"),acceptedMatches.getInt("Steps")
                ,acceptedMatches.getString("Name"),acceptedMatches.getInt("Wins"),
                acceptedMatches.getInt("Loses"),acceptedMatches.getString("Picture")));
        }
    }

    //get all the accepted trades from server
    private void getAcceptedTrades() throws JSONException, InterruptedException, ExecutionException, TimeoutException {
        acceptTrade = new ArrayList<>();
        HttpGetter getAcceptedTrades = new HttpGetter();
        getAcceptedTrades.execute(""+Configuration.currentUser.getId(),"AcceptedTrades");


        JSONObject json = new JSONObject(getAcceptedTrades.get(Configuration.timeoutTime, TimeUnit.SECONDS));
        JSONArray jsonUsers = json.getJSONArray("AcceptedTrades");

        for(int i=0;i<jsonUsers.length();i++)
        {

            JSONObject acceptedTrades = jsonUsers.getJSONObject(i);

            acceptTrade.add(new User(acceptedTrades.getInt("ID"),acceptedTrades.getInt("Steps")
                        ,acceptedTrades.getString("Name"),acceptedTrades.getInt("Wins"),
                    acceptedTrades.getInt("Loses"), acceptedTrades.getString("Picture")));
        }
    }

    //refresh if activity starting
    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    //if activity is ending stop the handler
    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

}

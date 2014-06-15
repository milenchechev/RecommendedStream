import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;


/**
 *
 * @author Milen
 */
public class CountUsersAndPosts {

    public static void main(String[] args) throws FileNotFoundException {
        File folder = new File("E:\\Personal\\PhD\\facebook\\RecommendedStream\\output");
        HashMap<String, LinkedList<String>> userToPostMap = new HashMap<String, LinkedList<String>>();
        HashMap<String, LinkedList<String>> postToUserMap = new HashMap<String, LinkedList<String>>();
        for (File file : folder.listFiles()) {
            Scanner in = new Scanner(file);
            while (in.hasNextLine()) {
                String[] line = in.nextLine().split("\\s+");
                if (line.length > 2) {
                    if (!postToUserMap.containsKey(line[0])) {
                        postToUserMap.put(line[0], new LinkedList<String>());
                        for (int i = 2; i < line.length; i++) {
                            if (!userToPostMap.containsKey(line[i])) {
                                userToPostMap.put(line[i], new LinkedList<String>());
                            }
                            userToPostMap.get(line[i]).add(line[0]);
                            postToUserMap.get(line[0]).add(line[i]);
                        }
                    }

                }
            }
        }
        final HashMap<String, LinkedList<String>> userToPostMapFinal = userToPostMap;
        final HashMap<String, LinkedList<String>> postToUserMapFinal = postToUserMap;
        String[] posts = new String[postToUserMap.size()];
        String[] users = new String[userToPostMap.size()];
        int i = 0 ;
        for(String post: postToUserMap.keySet()){
            posts[i] = post;
            i++;
        }
        i= 0 ;
        for(String user : userToPostMap.keySet()){
            users[i] = user;
            i++;
        }
        Arrays.sort(users,new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                if(userToPostMapFinal.get(o1).size() < userToPostMapFinal.get(o2).size()){
                    return 1;
                }
                return -1;
            } 
        });
        Arrays.sort(posts,new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                if(postToUserMapFinal.get(o1).size() < postToUserMapFinal.get(o2).size()){
                    return 1;
                }
                return -1;
            } 
        });
        //Arrays.sort(postPerUser);
        //Arrays.sort(usersPerPost);
        System.out.println( "users = "+users.length);
        System.out.println("posts = "+posts.length);
        for( i = 0 ; i < 100;i++){
            System.out.println("user= "+users[i]+ " likes="+userToPostMap.get(users[i]).size());
        }
        System.out.println("");
        for( i = 0 ; i < 100;i++){ 
            System.out.println("post= "+posts[i]+ " likes="+postToUserMap.get(posts[i]).size());
        }
        
        int usersAccum=0;
        for( i = 0 ; i < users.length;i++){
            usersAccum += userToPostMap.get(users[i]).size();
        }
        System.out.println(usersAccum/users.length);
        System.out.println("");
        int postsAccum = 0;
        for( i = 0 ; i < posts.length;i++){ 
            postsAccum += postToUserMap.get(posts[i]).size();
        }
        System.out.println("" + postsAccum/posts.length);
                
    }
}

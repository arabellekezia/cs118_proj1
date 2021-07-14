import java.sql.Timestamp;

public class PostInfo{
	public String username;
	public int postid;
	public String title;
	public String body;
	public Timestamp modified;
	public Timestamp created;

	public PostInfo(String username, int postid, String title, String body, Timestamp modified, Timestamp created){
		this.username = username;
		this.postid = postid;
		this.title = title;
		this.body = body;
		this.modified = modified;
		this.created = created;
	}
}

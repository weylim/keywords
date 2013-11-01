/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keywords;

/**
 *
 * @author WeeYong
 */
public class Sample {
    public int id;
    public String title;
    public String body;
    public String tags;
    Sample (int givenId, String givenTitle, String givenBody, String givenTags) {
        this.id = givenId;
        this.title = givenTitle;
        this.body = givenBody;
        this.tags = givenTags;
    }
}

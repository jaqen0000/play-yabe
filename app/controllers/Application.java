package controllers;

import play.*;
import play.cache.Cache;
import play.data.validation.Required;
import play.db.jpa.JPABase;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {
	@Before
	public static void addDefaults() {
		renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
		renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
	}

	public static void index() {
		Post frontPost = Post.find("order by postedAt desc").first();
		List<Post> olderPosts = Post.find("order by postedAt desc").from(1).fetch(10);
		render(frontPost, olderPosts);
	}

	public static void show(Long id) {
		Post post = Post.findById(id);
		String randomID = Codec.UUID();
		render(post, randomID);
	}

	public static void postComment(Long postId, @Required(message = "author is required") String author,
			@Required(message = "content is required") String content,
			@Required(message = "code is required") String code, String randomID) {
		Post post = Post.findById(postId);
		validation.equals(code.toLowerCase(), Cache.get(randomID)).message("Invalid code, please try it again");
		if (validation.hasErrors()) {
			render("Application/show.html", post, randomID);
		}
		post.addComment(author, content);
		flash.success("Thanks for posting %s", author);
		Cache.delete(randomID);
		show(postId);
	}

	public static void captcha(String id) {
		Images.Captcha captcha = Images.captcha();
		String code = captcha.getText("#E4EAFD").toLowerCase();
		Cache.set(id, code, "10mn");
		renderBinary(captcha);
	}

	public static void listTagged(String tag) {
		List<Post> posts = Post.findTaggedWith(tag);
		render(tag, posts);
	}
}
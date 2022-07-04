package com.springboot.moa.post;

import com.springboot.moa.post.model.GetPostDetailRes;
import com.springboot.moa.post.model.GetPostsRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PostDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetPostsRes> selectPosts(int categoryId){
        String selectPostsQuery = "\n" +
                "        SELECT p.post_id as post_id,\n" +
                "            p.user_id as user_id,\n" +
                "            p.point as point,\n" +
                "            p.title as title,\n" +
                "            p.content as content,\n" +
                "            p.deadline as deadline " +
                "        FROM post as p\n" +
                "            join category as c on p.category_id = c.category_id\n" +
                "where p.category_id=?";
        int selectPostsParam = categoryId;
        return this.jdbcTemplate.query(selectPostsQuery,
                (rs,rowNum) -> new GetPostsRes(
                        rs.getInt("post_id"),
                        rs.getInt("user_id"),
                        rs.getInt("point"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getInt("deadline")
                ), selectPostsParam);
    }
    public int checkCategoryExist(int categoryId){
        String checkCategoryExistQuery = "select exists(select category_id from category where category_id = ?)";
        int checkCategoryExistParams = categoryId;
        return this.jdbcTemplate.queryForObject(checkCategoryExistQuery,
                int.class,
                checkCategoryExistParams);

    }

    public List<GetPostDetailRes> selectPostDetail(int postId) {
        String selectPostDetailQuery = "\n" +
                "        SELECT \n" +
                "            pd.post_detail_id as post_detail_id,\n" +
                "            pd.question as question,\n" +
                "            pd.type as type\n" +
                "        FROM post_detail as pd\n" +
                "where pd.post_id=?";
        int selectPostDetailParam = postId;


        return this.jdbcTemplate.query(selectPostDetailQuery,
                (rs, rowNum) -> new GetPostDetailRes(
                        rs.getInt("post_detail_id"),
                        rs.getString("question"),
                        rs.getInt("type")
                ),selectPostDetailParam);

    }

    public int checkPostDetailExist(int postId) {
        String checkPostDetailQuery = "select exists(select post_id from post_detail where post_id = ?)";
        int checkPostDetailParams = postId;
        return this.jdbcTemplate.queryForObject(checkPostDetailQuery,
                int.class,
                checkPostDetailParams);

    }
}

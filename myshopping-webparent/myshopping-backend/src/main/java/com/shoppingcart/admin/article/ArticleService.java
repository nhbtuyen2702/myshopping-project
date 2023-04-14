package com.shoppingcart.admin.article;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shoppingcart.common.entity.Article;
import com.shoppingcart.common.entity.ArticleType;
import com.shoppingcart.common.entity.User;
import com.shoppingcart.common.exception.ArticleNotFoundException;

@Service
@Transactional
public class ArticleService {
	public static final int ARTICLES_PER_PAGE = 5;
	
	@Autowired private ArticleRepository repo;
	
	public Page<Article> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

		Pageable pageable = PageRequest.of(pageNum - 1, ARTICLES_PER_PAGE, sort);

		if (keyword != null) {
			return repo.findAll(keyword, pageable);
		}

		return repo.findAll(pageable);
	}

	
	public List<Article> listAll() {
		return repo.findPublishedArticlesWithIDAndTitleOnly();
	}
	
	public List<Article> listArticlesForMenu() {
		return repo.findByTypeOrderByTitle(ArticleType.MENU_BOUND);
	}
	
	public void save(Article article, User user) {
		setDefaultAlias(article);
				
		article.setUpdatedTime(new Date());
		article.setUser(user);
		
		repo.save(article);
	}
	
	private void setDefaultAlias(Article article) {
		if (article.getAlias() == null || article.getAlias().isEmpty()) {
			article.setAlias(article.getTitle().replaceAll(" ", "-"));
		}		
	}
	
	public Article get(Integer id) throws ArticleNotFoundException {
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new ArticleNotFoundException("Could not find any article with ID " + id);
		}
	}	
	
	public void updatePublishStatus(Integer id, boolean published) throws ArticleNotFoundException {
		if (!repo.existsById(id)) {
			throw new ArticleNotFoundException("Could not find any article with ID " + id);
		}
		repo.updatePublishStatus(id, published);
	}
	
	public void delete(Integer id) throws ArticleNotFoundException {
		if (!repo.existsById(id)) {
			throw new ArticleNotFoundException("Could not find any article with ID " + id);			
		}
		repo.deleteById(id);
	}	
}

package com.shoppingcart.client.product;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.shoppingcart.client.ControllerHelper;
import com.shoppingcart.client.category.CategoryService;
import com.shoppingcart.client.question.QuestionService;
import com.shoppingcart.client.question.vote.QuestionVoteService;
import com.shoppingcart.client.review.ReviewService;
import com.shoppingcart.client.review.vote.ReviewVoteService;
import com.shoppingcart.common.entity.Category;
import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.common.entity.Question;
import com.shoppingcart.common.entity.Review;
import com.shoppingcart.common.entity.product.Product;
import com.shoppingcart.common.exception.CategoryNotFoundException;
import com.shoppingcart.common.exception.ProductNotFoundException;

@Controller
public class ProductController {
	
	@Autowired private ProductService productService;
	@Autowired private CategoryService categoryService;
	@Autowired private ReviewService reviewService;	
	@Autowired private ReviewVoteService reviewVoteService;
	@Autowired private QuestionVoteService questionVoteService;
	@Autowired private ControllerHelper controllerHelper;
	@Autowired private QuestionService questionService;


	//khi nhấn vào category bất kỳ
	@GetMapping("/c/{category_alias}")
	public String viewCategoryFirstPage(@PathVariable("category_alias") String alias,
			Model model) {
		return viewCategoryByPage(alias, 1, model);
	}
	
	@GetMapping("/c/{category_alias}/page/{pageNum}")
	public String viewCategoryByPage(@PathVariable("category_alias") String alias,
			@PathVariable("pageNum") int pageNum,
			Model model) {
		try {
			Category category = categoryService.getCategory(alias);		
			List<Category> listCategoryParents = categoryService.getCategoryParents(category);//lấy ra tất cả categories cha,ông,...của category hiện tại
			
			Page<Product> pageProducts = productService.listByCategory(pageNum, category.getId());//lấy ra tất cả products thuộc về category hiện tại và tất cả products thuộc về categories con,cháu,... của category hiện tại
			List<Product> listProducts = pageProducts.getContent();
			
			long startCount = (pageNum - 1) * ProductService.PRODUCTS_PER_PAGE + 1;
			long endCount = startCount + ProductService.PRODUCTS_PER_PAGE - 1;
			if (endCount > pageProducts.getTotalElements()) {
				endCount = pageProducts.getTotalElements();
			}
			
			model.addAttribute("currentPage", pageNum);
			model.addAttribute("totalPages", pageProducts.getTotalPages());
			model.addAttribute("startCount", startCount);
			model.addAttribute("endCount", endCount);
			model.addAttribute("totalItems", pageProducts.getTotalElements());
			model.addAttribute("pageTitle", category.getName());
			model.addAttribute("listCategoryParents", listCategoryParents);
			model.addAttribute("listProducts", listProducts);
			model.addAttribute("category", category);
			
			return "product/products_by_category";
		} catch (CategoryNotFoundException ex) {
			return "error/404";
		}
	}
	
	@GetMapping("/p/{product_alias}")
	public String viewProductDetail(@PathVariable("product_alias") String alias, Model model,
			HttpServletRequest request) {
		try {
			Product product = productService.getProduct(alias);
			List<Category> listCategoryParents = categoryService.getCategoryParents(product.getCategory());//lấy ra tất cả categories cha,ông,...của category hiện tại
			List<Question> listQuestions = questionService.getTop3VotedQuestions(product.getId());
			Page<Review> listReviews = reviewService.list3MostVotedReviewsByProduct(product);//lấy ra 3 reviews cho product hiện tại
			
			Customer customer = controllerHelper.getAuthenticatedCustomer(request);
			
			if (customer != null) {
				boolean customerReviewed = reviewService.didCustomerReviewProduct(customer, product.getId());
				reviewVoteService.markReviewsVotedForProductByCustomer(listReviews.getContent(), product.getId(), customer.getId());//nếu customer hiện tại đã từng vote cho product này -->sẽ in đậm thumb up hoặc thumb down
				questionVoteService.markQuestionsVotedForProductByCustomer(listQuestions, product.getId(), customer.getId());
				
				//gán customerCanReview cho product hiện tại
				//trong trang review_form nếu customerCanReview = true -->You already reviewed this product.
				//trong trang product_top_reviews nếu customerCanReview = true -->You already reviewed this product.
				if (customerReviewed) {
					model.addAttribute("customerReviewed", customerReviewed);
				} else {
					boolean customerCanReview = reviewService.canCustomerReviewProduct(customer, product.getId());
					model.addAttribute("customerCanReview", customerCanReview);
				}
			}
			
			int numberOfQuestions = questionService.getNumberOfQuestions(product.getId());
			int numberOfAnsweredQuestions = questionService.getNumberOfAnsweredQuestions(product.getId());
			
			model.addAttribute("listQuestions", listQuestions);			
			model.addAttribute("numberOfQuestions", numberOfQuestions);
			model.addAttribute("numberOfAnsweredQuestions", numberOfAnsweredQuestions);
			
			model.addAttribute("listCategoryParents", listCategoryParents);
			model.addAttribute("product", product);
			model.addAttribute("listReviews", listReviews);
			model.addAttribute("pageTitle", product.getShortName());
			
			return "product/product_detail";
		} catch (ProductNotFoundException e) {
			return "error/404";
		}
	}
	
	@GetMapping("/search")
	public String searchFirstPage(String keyword, Model model) {
		return searchByPage(keyword, 1, model);
	}
	
	@GetMapping("/search/page/{pageNum}")
	public String searchByPage(String keyword, @PathVariable("pageNum") int pageNum, Model model) {
		Page<Product> pageProducts = productService.search(keyword, pageNum);//dùng FULL TEXT SEARCH trong SQL
		List<Product> listResult = pageProducts.getContent();//khi bấm search thì chỉ cần lấy ra tất cả products có name,short_description,full_description trùng với keyword, ko lấy ra categories
		
		long startCount = (pageNum - 1) * ProductService.SEARCH_RESULTS_PER_PAGE + 1;
		long endCount = startCount + ProductService.SEARCH_RESULTS_PER_PAGE - 1;
		if (endCount > pageProducts.getTotalElements()) {
			endCount = pageProducts.getTotalElements();
		}
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageProducts.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", pageProducts.getTotalElements());
		model.addAttribute("pageTitle", keyword + " - Search Result");
		
		model.addAttribute("keyword", keyword);
		model.addAttribute("searchKeyword", keyword);
		model.addAttribute("listResult", listResult);
		
		return "product/search_result";
	}		
	
}

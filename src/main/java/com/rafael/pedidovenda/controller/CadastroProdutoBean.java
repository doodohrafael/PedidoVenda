package com.rafael.pedidovenda.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import com.rafael.pedidovenda.model.Categoria;
import com.rafael.pedidovenda.model.Produto;
import com.rafael.pedidovenda.repository.Categorias;
import static com.rafael.pedidovenda.util.jsf.FacesUtil.isNotPostback;

import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class CadastroProdutoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Categorias categorias;

	@Getter
	private Produto produto;
	
	@Setter 
	private Categoria categoriaPai;
	
	@Getter
	private List<Categoria> categoriasRaizes;
	@Getter
	private List<Categoria> subcategorias;
	
	public CadastroProdutoBean() {
		produto = new Produto();
	}

	public void inicializar() {
		if(isNotPostback()) {
			categoriasRaizes = categorias.raizes();
		}
	}

	public void salvar() {
		System.err.println("Categoria pai selecionada: " + 
				categoriaPai.getDescricao());
		System.err.println("Subcategoria selecionada: " + 
				produto.getCategoria().getDescricao());
	}
	
	public void carregarSubcategorias() {
		subcategorias = categorias.subcategorias(categoriaPai);
	}
	
	@NotNull
	public Categoria getCategoriaPai() {
		return categoriaPai;
	}

}

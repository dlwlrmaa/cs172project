import React, { Component } from 'react'

export default class searchBar extends Component {
	constructor(props) {
		super(props)
		this.state = {
			search: ''
		}
	}

	handleSubmit = (event) => {
		event.preventDefault()
		const query = this.state.search
		const baseURL = 'http://localhost:8080'
		const searchURL = baseURL + '/search?query="' + query + '"'
		console.log(searchURL.toString())
		fetch(searchURL)
		 .then(res => res.json())
		 .then((data) => {
		 	this.setState({searchResults: data})
		 	console.log(this.state)
		 })
		 .catch(console.log)
	}

	handleInputChange = (event) => {
		event.preventDefault()
		this.setState({
			search: event.target.value
		})
	}

	render () {
		const {search} = this.state
		return (
			<div>
				<h1>Search Bar</h1>
				<p>Query is: {search}</p>
				<form onSubmit={this.handleSubmit}>
					<p><input type='text' placeholder='Search a Tweet' name='name' onChange={this.handleInputChange}/></p>
					<p><button>Search</button></p>
				</form>
			</div>
		)
	}
}
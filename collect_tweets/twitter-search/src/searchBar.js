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
				<p>Search is: {search}</p>
				<form onSubmit={this.handleSubmit}>
					<p><input type='text' placeholder='Search a Tweet' name='name' onChange={this.handleInputChange}/></p>
					<p><button>Search</button></p>
				</form>
			</div>
		)
	}
}
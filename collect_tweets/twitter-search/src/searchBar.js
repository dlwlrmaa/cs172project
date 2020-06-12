import React, { Component } from 'react'

export default class searchBar extends Component {
	constructor(props) {
		super(props)
		this.state = {
			search: '',
			tweets: []
		}
	}

	handleSubmit = (event) => {
		event.preventDefault()
		const query = this.state.search
		const baseURL = 'http://localhost:8080'
		const searchURL = baseURL + '/search?query="' + query + '"'
		//console.log(data[0].fields))
		fetch(searchURL)
		 .then(res => res.json())
		 .then((data) => 
		 	this.setState({tweets: data}))
		 .then(console.log("this.state.tweets"))
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
					<p><input type='text' autocomplete="off" placeholder='Search a Tweet' name='name' onChange={this.handleInputChange} /></p>
					<p><button>Search</button></p>
				</form>
				<ul>
					{this.state.tweets.map(x => 
						<li style={{textAlign:"left", fontWeight:"bold"}}>
							{x.fields[1].charSequenceValue}
							<br></br>
							{x.fields[5].charSequenceValue}
							<ul style={{fontWeight:"normal"}}>
								{x.fields[2].charSequenceValue}
							</ul>
							<hr></hr>
						</li>
					)}
					
				</ul>
			</div>
		)
	}
}